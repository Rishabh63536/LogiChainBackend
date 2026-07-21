package com.cts.logichain360.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTUtil {
	//key being used to sign tokens
	@Value("${SECRET_KEY}")
    private String SECRET_KEY;

	//converts secret to cryptographic key object tht jwt lib understands(mathematical stamp only server can produce)
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    //extracts username from token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    //extracts expirationDate form token
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    //verification step, when parseSignedClaims() is called it recomputes signature using private key and matches
    //with whats in token, helps in avoiding tampering
    //if same then return the payload(Claim)
    //if not then return an exception
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    //returns true if token expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //called after a successful login, takes username as input and gives out a jwt string
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    //fn where jwt is built. subject is username
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 50)) // 5 minutes expiration time
                .signWith(getSigningKey())
                .compact();
        //.compact() serializes everything into final string
    }
    
    //checks if token has expired
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
