package com.aigateway.security;

import com.aigateway.config.AppProperties;
import com.aigateway.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppProperties props;
    private final SecretKey key;

    public JwtService(AppProperties props) {
        this.props = props;
        String secret = props.getSecurity().getJwt().getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be set and be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(props.getSecurity().getJwt().getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(props.getSecurity().getJwt().getIssuer())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("plan", user.getPlan().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.getSecurity().getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
