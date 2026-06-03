package ufps.edu.co.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class PasswordResetTokenService {

    private static final String TOKEN_TYPE = "PASSWORD_RESET";
    private static final long EXPIRATION_MS = 3_600_000L;

    private final SecretKey key;

    public PasswordResetTokenService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String correo) {
        Date now = new Date();
        return Jwts.builder()
                .subject(correo)
                .claim("type", TOKEN_TYPE)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public String validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!TOKEN_TYPE.equals(claims.get("type", String.class))) {
                throw new IllegalArgumentException("Token inválido: tipo incorrecto");
            }
            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            throw new IllegalArgumentException("El enlace de recuperación ha expirado", ex);
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Token de recuperación inválido", ex);
        }
    }
}
