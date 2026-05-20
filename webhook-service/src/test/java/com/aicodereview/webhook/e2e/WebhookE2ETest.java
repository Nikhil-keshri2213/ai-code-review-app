package com.aicodereview.webhook.e2e;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment env;

    // Mock Kafka so CI runner doesn't need a real broker
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private WireMockServer wireMockServer;

    private static final String SECRET = "my-webhook-secret";

    private static final String PAYLOAD = """
            {"action":"opened","number":1,
             "pull_request":{"title":"Test","head":{"sha":"abc123","ref":"feature"},
             "base":{"sha":"def456","ref":"main"}},
             "repository":{"id":1,"name":"test-repo",
             "full_name":"test-org/test-repo","private":false},
             "sender":{"login":"testuser","id":1}}
            """;

    @BeforeEach
    void startWireMock() {
        System.out.println("Loaded secret = " + env.getProperty("github.webhook.secret"));

        // Make KafkaTemplate.send() return a successful future instead of hitting broker
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        wireMockServer = new WireMockServer(wireMockConfig().port(8099));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8099);

        stubFor(get(urlPathMatching("/repos/.*/pulls/1/files"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [{"filename":"src/Main.java",
                                  "status":"modified",
                                  "patch":"@@ -1,3 +1,4 @@\\n public class Main {\\n+    int x = 1;\\n }",
                                  "additions":1,
                                  "deletions":0}]
                                """)));

        stubFor(get(urlPathMatching("/repos/.*/contents/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("public class Main { int x = 1; }")));
    }

    @AfterEach
    void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void webhook_validSignature_returns200() throws Exception {
        String signature = computeHmac(PAYLOAD.trim(), SECRET);

        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-Hub-Signature-256", signature)
                        .content(PAYLOAD.trim()))
                .andExpect(status().isOk());
    }

    @Test
    void webhook_invalidSignature_returns401() throws Exception {
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-Hub-Signature-256", "sha256=invalidsignature")
                        .content(PAYLOAD.trim()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhook_missingSignature_returns401() throws Exception {
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .content(PAYLOAD.trim()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhook_wrongEvent_ignoredReturns200() throws Exception {
        String signature = computeHmac(PAYLOAD.trim(), SECRET);

        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", signature)
                        .content(PAYLOAD.trim()))
                .andExpect(status().isOk());
    }

    private String computeHmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return "sha256=" + hex;
    }
}