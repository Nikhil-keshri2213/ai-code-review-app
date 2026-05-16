package com.aicodereview.fetch.service;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.fetch.util.LanguageDetector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"code-analysis-tasks"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:19092",
                        "port=19092"}
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "kafka.topics.code-analysis-tasks=code-analysis-tasks",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
class CodeFetchServiceKafkaTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void publishReviewRequest_canBeConsumed() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .correlationId(UUID.randomUUID().toString())
                .repoFullName("test/repo")
                .prNumber(1)
                .fileName("Test.java")
                .language("java")
                .fileContent("public class Test {}")
                .diffContent("")
                .chunkIndex(0)
                .totalChunks(1)
                .isChunked(false)
                .build();

        kafkaTemplate.send("code-analysis-tasks", "Test.java", request);

        // Set up consumer
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:19092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        KafkaConsumer<String, String> consumer =
                new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("code-analysis-tasks"));

        var records = consumer.poll(Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).isGreaterThan(0);
        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.value()).contains("test/repo");
        assertThat(record.value()).contains("Test.java");
    }

    @Test
    void languageDetector_java_returnsJava() {
        assertThat(LanguageDetector.detect("UserService.java")).isEqualTo("java");
    }

    @Test
    void languageDetector_python_returnsPython() {
        assertThat(LanguageDetector.detect("main.py")).isEqualTo("python");
    }

    @Test
    void languageDetector_unknown_returnsUnknown() {
        assertThat(LanguageDetector.detect("README.xyz")).isEqualTo("unknown");
    }
}