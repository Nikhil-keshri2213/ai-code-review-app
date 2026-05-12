package com.aicodereview.webhook.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubSignatureValidatorTest {

    private GitHubSignatureValidator validator;
    private static final String SECRET = "test-webhook-secret";
    private static final String PAYLOAD = "{\"action\":\"opened\",\"number\":1}";

    @BeforeEach
    void setUp() {
        validator = new GitHubSignatureValidator();
        // inject secret via reflection or constructor depending on your impl
        org.springframework.test.util.ReflectionTestUtils
                .setField(validator, "webhookSecret", SECRET);
    }

    @Test
    void isValid_correctSignature_returnsTrue() throws Exception {
        String signature = computeHmac(PAYLOAD, SECRET);

        boolean result = validator.isValid(
                PAYLOAD.getBytes(StandardCharsets.UTF_8), signature);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_wrongSignature_returnsFalse() {
        boolean result = validator.isValid(
                PAYLOAD.getBytes(StandardCharsets.UTF_8),
                "sha256=wrongsignaturevalue");

        assertThat(result).isFalse();
    }

    @Test
    void isValid_nullSignature_returnsFalse() {
        boolean result = validator.isValid(
                PAYLOAD.getBytes(StandardCharsets.UTF_8), null);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_wrongPrefix_returnsFalse() throws Exception {
        String validHmac = computeHmac(PAYLOAD, SECRET);
        // Replace sha256= prefix with md5=
        String wrongPrefix = "md5=" + validHmac.substring(7);

        boolean result = validator.isValid(
                PAYLOAD.getBytes(StandardCharsets.UTF_8), wrongPrefix);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_emptyPayload_withCorrectSignature_returnsTrue() throws Exception {
        byte[] emptyPayload = new byte[0];
        String signature = computeHmacBytes(emptyPayload, SECRET);

        boolean result = validator.isValid(emptyPayload, signature);

        assertThat(result).isTrue();
    }

    private String computeHmac(String payload, String secret) throws Exception {
        return computeHmacBytes(
                payload.getBytes(StandardCharsets.UTF_8), secret);
    }

    private String computeHmacBytes(byte[] payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return "sha256=" + hex;
    }
}