package br.com.bdregistros.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Emite e valida tokens JWT assinados com HMAC. Exige JWT_SECRET explicito
 * (32+ caracteres) para nao rodar com uma chave fraca ou padrao — mesma
 * logica do AdminBootstrap: falhar cedo em vez de usar um valor inseguro.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${JWT_SECRET:}") String secret,
                       @Value("${JWT_EXPIRATION_MINUTES:120}") long expirationMinutes) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa estar definido como variavel de ambiente com pelo menos 32 caracteres.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String gerarToken(String login, String papel) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(login)
                .claim("papel", papel)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public Claims validarEExtrair(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
