package com.ll.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.ll.model.PersonalAccount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private static final long EXPIRATION_TIME = 864_000_000; // 10 days
  private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  public String createToken(PersonalAccount account) {
    return Jwts.builder()
        .setSubject(account.getId())
        .claim("type", "personal")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key)
        .compact();
  }

  public Claims validateToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
