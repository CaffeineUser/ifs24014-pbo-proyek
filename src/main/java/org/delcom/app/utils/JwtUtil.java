package org.delcom.app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.delcom.app.entities.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Secret Key (Harus panjang/kuat untuk algoritma SHA-256)
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    // Method untuk membuat SecretKey object dari String
    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==========================================
    // GENERATE TOKEN
    // ==========================================
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Menyimpan data tambahan (Role & Nama) ke dalam payload token
        claims.put("role", user.getRole());
        claims.put("name", user.getName());
        
        return createToken(claims, user.getId().toString());
    }

    private static String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)              // Set custom claims (role, name)
                .subject(subject)            // Set Subject (User ID)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Jam
                .signWith(getSignInKey(), Jwts.SIG.HS256) // Syntax Baru 0.12.x
                .compact();
    }

    // ==========================================
    // VALIDATE TOKEN
    // ==========================================
    public static Boolean validateToken(String token, boolean isHardCheck) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ==========================================
    // EXTRACT DATA
    // ==========================================
    public static UUID extractUserId(String token) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            return UUID.fromString(subject);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllClaims(String token) {
        // Syntax Baru 0.12.x: parser() menggantikan parserBuilder()
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Gunakan verifyWith, bukan setSigningKey
                .build()
                .parseSignedClaims(token)   // Gunakan parseSignedClaims, bukan parseClaimsJws
                .getPayload();              // Gunakan getPayload(), bukan getBody()
    }
}