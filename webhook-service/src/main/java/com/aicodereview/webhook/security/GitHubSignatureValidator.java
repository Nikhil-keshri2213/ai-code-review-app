package com.aicodereview.webhook.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class GitHubSignatureValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "sha256=";

    @Value("${github.webhook.secret:my-webhook-secret}")
    private String webhookSecret;

    public boolean isValid(byte[] payload, String signatureHeader) {

        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Missing X-Hub-Signature-256 header");
            return false;
        }

        if (!signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            log.warn("Malformed X-Hub-Signature-256 header");
            return false;
        }

        try {

            String receivedSignature =
                    signatureHeader.substring(SIGNATURE_PREFIX.length());

            String computedSignature =
                    computeHmacSha256(payload);

            // hex malformed protection
            byte[] receivedBytes =
                    hexStringToByteArray(receivedSignature);

            byte[] computedBytes =
                    hexStringToByteArray(computedSignature);

            if (receivedBytes == null || computedBytes == null) {
                log.warn("Invalid signature format");
                return false;
            }

            boolean valid =
                    MessageDigest.isEqual(
                            receivedBytes,
                            computedBytes
                    );

            if (!valid) {
                log.warn("Signature mismatch — request rejected");
            }

            return valid;

        } catch (Exception e) {
            log.error(
                    "Signature validation error",
                    e
            );
            return false;
        }
    }

    private String computeHmacSha256(byte[] payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);

        SecretKeySpec secretKey =
                new SecretKeySpec(
                        webhookSecret.getBytes(StandardCharsets.UTF_8),
                        HMAC_ALGORITHM
                );

        mac.init(secretKey);

        byte[] hash = mac.doFinal(payload);

        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }

    private byte[] hexStringToByteArray(String hex) {

        if (hex == null) {
            return null;
        }

        // odd length protection
        if (hex.length() % 2 != 0) {
            return null;
        }

        int len = hex.length();

        byte[] data = new byte[len / 2];

        try {

            for (int i = 0; i < len; i += 2) {

                int first =
                        Character.digit(
                                hex.charAt(i),
                                16
                        );

                int second =
                        Character.digit(
                                hex.charAt(i + 1),
                                16
                        );

                if (first == -1 || second == -1) {
                    return null;
                }

                data[i / 2] =
                        (byte) ((first << 4) + second);
            }

            return data;

        } catch (Exception e) {
            return null;
        }
    }
}