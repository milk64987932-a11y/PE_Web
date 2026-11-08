package com.umc.movie.config;

import com.umc.movie.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
  private final SecretKey key;
  private final long expiration;

  public JwtProvider(@Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration}") long expiration) {
    try {
      byte[] bytes = Decoders.BASE64.decode(secret);
      if (bytes.length < 32 || expiration <= 0) throw new IllegalArgumentException();
      key = Keys.hmacShaKeyFor(bytes);
      this.expiration = expiration;
    } catch (Exception e) {
      throw new IllegalArgumentException("JWT_SECRET은 32바이트 이상 난수의 Base64 값이고 만료 시간은 양수여야 합니다.");
    }
  }

  public String create(Long memberId) {
    Instant now = Instant.now();
    return Jwts.builder().subject(memberId.toString()).issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expiration))).signWith(key, Jwts.SIG.HS256).compact();
  }

  public Long verify(String token) {
    try {
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      Long memberId = Long.valueOf(claims.getSubject());
      if (memberId <= 0 || claims.getExpiration() == null) throw new IllegalArgumentException();
      return memberId;
    } catch (ExpiredJwtException e) {
      throw new ApiException(401, "AUTH401_4", "만료된 토큰입니다.");
    } catch (JwtException | IllegalArgumentException e) {
      throw new ApiException(401, "AUTH401_3", "유효하지 않은 토큰입니다.");
    }
  }
}
