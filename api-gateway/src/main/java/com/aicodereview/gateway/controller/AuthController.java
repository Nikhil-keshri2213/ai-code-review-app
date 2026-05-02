package com.aicodereview.gateway.controller;

import com.aicodereview.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/token")
    public Mono<ResponseEntity<Map<String, String>>> getToken(
            @RequestBody Mono<Map<String, String>> credentialsMono) {

        return credentialsMono.map(credentials -> {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if ("admin".equals(username) && "password".equals(password)) {
                String token = jwtUtil.generateToken(username);
                log.info("✅ Token issued for user: {}", username);
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "type", "Bearer",
                        "user", username
                ));
            }

            log.warn("❌ Invalid credentials for user: {}", username);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .<Map<String, String>>build();
        });
    }
}