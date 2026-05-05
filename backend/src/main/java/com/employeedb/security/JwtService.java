package com.employeedb.security;

import com.employeedb.model.AppUser;
import com.employeedb.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-ms}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(sha256(secret));
    this.expirationMs = expirationMs;
  }

  private static byte[] sha256(String secret) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public String generateToken(AppUser user) {
    Date now = new Date();
    return Jwts.builder()
        .subject(user.getUsername())
        .claim("role", user.getRole().name())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return parse(token).getSubject();
  }

  public Role extractRole(String token) {
    String r = parse(token).get("role", String.class);
    return r != null ? Role.valueOf(r) : Role.USER;
  }

  public boolean isValid(String token, String expectedUsername) {
    try {
      Claims c = parse(token);
      return c.getSubject().equals(expectedUsername) && c.getExpiration().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
