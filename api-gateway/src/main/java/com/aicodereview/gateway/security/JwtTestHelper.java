package com.aicodereview.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

public class JwtTestHelper {

    private static final String JWT_SECRET =
            "bXlTdXBlclNlY3JldEtleUZvckpXVEF1dGhlbnRpY2F0aW9uMjAyNA==";

    public static void main(String[] args) {

        byte[] keyBytes =
                Base64.getDecoder().decode(JWT_SECRET);

        Key key = Keys.hmacShaKeyFor(keyBytes);

        long now = System.currentTimeMillis();

        String validToken = Jwts.builder()
                .setSubject("audit-user")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String expiredToken = Jwts.builder()
                .setSubject("audit-user")
                .setIssuedAt(new Date(now - 7200000))
                .setExpiration(new Date(now - 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String[] parts = validToken.split("\\.");

        String tamperedPayload =
                parts[1].substring(0, parts[1].length()-1)
                + "A";

        String tamperedToken =
                parts[0]+"."+tamperedPayload+"."+parts[2];

        System.out.println("\nVALID:");
        System.out.println(validToken);

        System.out.println("\nEXPIRED:");
        System.out.println(expiredToken);

        System.out.println("\nTAMPERED:");
        System.out.println(tamperedToken);
    }
}