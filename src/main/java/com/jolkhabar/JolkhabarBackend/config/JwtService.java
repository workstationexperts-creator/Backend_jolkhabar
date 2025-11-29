package com.jolkhabar.JolkhabarBackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret.path:jwt-secret.key}")
    private String keyFilePath;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Key signingKey;

    // ======================================================
    // ✅ Proper lifecycle initialization
    // ======================================================
    @PostConstruct
    public void init() {
        this.signingKey = loadOrCreateKey();
    }

    // ======================================================
    // ✅ Load or create key dynamically
    // ======================================================
    private Key loadOrCreateKey() {
        try {
            Path path = Path.of(keyFilePath);

            if (Files.exists(path)) {
                // Reuse existing key
                String encoded = Files.readString(path).trim();
                byte[] keyBytes = Decoders.BASE64.decode(encoded);
                return Keys.hmacShaKeyFor(keyBytes);
            } else {
                // Generate new key
                byte[] newKey = new byte[32];
                new Random().nextBytes(newKey);
                String encoded = Base64.getEncoder().encodeToString(newKey);
                Files.writeString(path, encoded);
                return Keys.hmacShaKeyFor(newKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or generate JWT key", e);
        }
    }

    // ======================================================
    // ✅ Token generation
    // ======================================================
    public String generateToken(String username, Collection<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", roles);
        return buildToken(claims, username, jwtExpirationMs);
    }

    public String generateToken(UserDetails userDetails) {
        Collection<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return generateToken(userDetails.getUsername(), roles);
    }

    // ======================================================
    // ✅ Validation
    // ======================================================
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
