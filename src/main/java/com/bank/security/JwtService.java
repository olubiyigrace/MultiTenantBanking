package com.bank.security;

import com.bank.exceptions.InvalidRequestException;
import com.bank.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtProperties jwtProperties;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            privateKey = loadPrivateKey(jwtProperties.getPrivateKeyPath());
            publicKey = loadPublicKey(jwtProperties.getPublicKeyPath());

            log.info("Private & Public key loaded successfully");
        } catch (final Exception e) {
            log.error("Error loading private key", e);
            throw new RuntimeException("Error loading private key", e);
        }
    }

    public String generateAccessToken(@Nonnull String institutionId, String userId, String sessionId, String userAccountType) {
        Date now = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(institutionId)
                .claim("user_id", userId)
                .claim("sid", sessionId)
                .claim("user_account_type", userAccountType)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiration)
                .issuer("multitenantbank-app")
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(@Nonnull String institutionId, @Nonnull String userId, String sessionId, String userAccountType) {
        Date now = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration());
        return Jwts.builder()
                .subject(userId)
                .claim("institution_id", institutionId)
                .claim("sid", sessionId)
                .claim("user_account_type", userAccountType)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .issuer("multitenantbank-app")
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();

    }
    public String getUserIdFromRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("user_id", String.class);
    }

    public String getInstitutionIdFromToken(String token) {
       Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getUserAccountTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("user_account_type", String.class);
    }

    public boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
        return true;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        System.out.println("JWT CLAIMS = " + claims);
        return claims;
    }

    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return false;

        return "refresh".equals(String.valueOf(claims.get("tokenType")));
    }

    public boolean isAccessToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return false;
        return "access".equals(claims.get("tokenType", String.class));
    }

    public String generateLoginToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (5 * 60 * 1000));
        return Jwts.builder()
                .subject(userId)
                .claim("type", "login_token")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public void validateLoginToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("type", String.class);
        if (!"login_token".equals(type)) {
            throw new InvalidRequestException("Invalid login token");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new InvalidRequestException("Login token expired");
        }
    }

    public String getUserIdFromLoginToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getSessionId(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("sid", String.class);
    }

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("purpose", "PASSWORD_RESET")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean validatePasswordResetToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("purpose", String.class);
        if (!"PASSWORD_RESET".equals(type)) {
            throw new InvalidRequestException("Invalid reset token");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new InvalidRequestException("Reset token expired");
        }
        return true;
    }

    public String getEmailFromResetToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String purpose = claims.get("purpose", String.class);
        if (!"PASSWORD_RESET".equals(purpose)) {
            throw new InvalidRequestException("Invalid reset token");
        }
        return claims.getSubject();
    }

    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        try (InputStream is = JwtService.class.getClassLoader()
                .getResourceAsStream(privateKeyPath)) {

            if (is == null) {
                throw new RuntimeException("Private key not found");
            }

           String key = new String(is.readAllBytes());
           String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

           byte[] encoded = Base64.getDecoder()
                    .decode(privateKeyPEM);
           PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey(String publicKeyPath) throws Exception {
        try (InputStream is = JwtService.class.getClassLoader()
                .getResourceAsStream(publicKeyPath)) {
            if (is == null) {
                throw new RuntimeException("Public key not found");
            }
            String key = new String(is.readAllBytes());
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encoded = Base64.getDecoder()
                    .decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return KeyFactory.getInstance("RSA")
                    .generatePublic(keySpec);
        }
    }
}