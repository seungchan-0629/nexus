package com.example.nexus.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;


public class JwtUtil {
    static String key = "dsfaijasdifjasdilfknasffi3iln39f09asdf0as8df09230u9hasfj92j31j1fnaldfn1";
    static SecretKey encodedKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));

    public static String createToken(Long idx, String email, String role) {
        String jwt = Jwts.builder()
                .claim("idx", idx)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+ 86400000))
                .signWith(encodedKey)
                .compact();

        return jwt;
    }

    public static Long getUserIdx(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(encodedKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("idx", Long.class);
    }

    public static String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(encodedKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    public static String getRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(encodedKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }
}
