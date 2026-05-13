package com.bank.services;

import com.bank.utils.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    public TokenPair generateTokenPair(Authentication authentication){
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);
        return new TokenPair(accessToken,refreshToken);
    }
    public String generateAccessToken(Authentication authentication){
        return generateToken(authentication,jwtExpirationMs, new HashMap<>());
    }
    public String generateRefreshToken(Authentication authentication){
        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return generateToken(authentication, jwtRefreshExpirationMs, claims);
    }
    public String generateToken(Authentication authentication, long jwtExpirationMs, Map<String, String> claims){
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        assert userPrincipal != null;
        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }
    public boolean validateTokenForUser(String token, UserDetails userDetails){
        final String username = extractUsernameFromToken(token);
        return username != null && username.equals(userDetails.getUsername());
    }
    public boolean isValidToken(String token){
        return extractAllClaims(token) != null;
    }
    public String extractUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }
    public boolean isRefreshToken(String token){
        Claims claims = extractAllClaims(token);
        if(claims == null){
            return false;
        }
        return "refresh".equals(claims.get("tokenType"));
    }
    private Claims extractAllClaims(String token){
        Claims claims = null;
        try{
            claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e){
            throw new RuntimeException(e);
        }
        return claims;
    }
    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public long getRemainingValidity(String token){
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
