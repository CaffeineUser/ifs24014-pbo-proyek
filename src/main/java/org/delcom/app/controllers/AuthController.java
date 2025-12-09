package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.delcom.app.dto.LoginRequest;
import org.delcom.app.dto.LoginResponse;
import org.delcom.app.dto.RegisterRequest;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // =================================================================
    // LOGIN
    // =================================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            // 1. Validasi Email & Password (Logic ada di UserService)
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            // 2. Generate Token JWT
            String token = JwtUtil.generateToken(user);

            // 3. Simpan Token ke Database (Untuk fitur Logout & Security)
            authService.saveUserToken(user.getId(), token);

            // 4. SET COOKIE (PENTING untuk Thymeleaf/Browser)
            // Ini membuat browser otomatis membawa token di setiap request halaman
            createAuthCookie(response, token, 24 * 60 * 60); // Expired 1 hari (detik)

            // 5. Return JSON (Untuk Client API/JS)
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getName(),
                    user.getRole().name()
            ));

        } catch (RuntimeException e) {
            // Tangkap error jika email/password salah
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login Gagal: " + e.getMessage());
        }
    }

    // =================================================================
    // REGISTER
    // =================================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Mapping dari DTO ke Entity User
            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(request.getPassword()); // UserService akan melakukan hashing
            newUser.setPhone(request.getPhone());
            newUser.setAddress(request.getAddress());

            // Simpan user baru
            userService.registerUser(newUser);

            return ResponseEntity.ok("Registrasi berhasil. Silakan login.");
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Registrasi Gagal: " + e.getMessage());
        }
    }

    // =================================================================
    // LOGOUT
    // =================================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Ambil token dari header atau cookie
        String token = extractToken(request);

        // 2. Hapus token dari database
        if (token != null) {
            authService.logout(token);
        }

        // 3. HAPUS COOKIE di Browser (Timpa dengan cookie kosong umur 0 detik)
        createAuthCookie(response, null, 0);

        return ResponseEntity.ok("Logout berhasil");
    }

    // =================================================================
    // HELPER METHODS
    // =================================================================

    private void createAuthCookie(HttpServletResponse response, String token, int maxAge) {
        Cookie cookie = new Cookie("AUTH_TOKEN", token);
        
        cookie.setHttpOnly(true); // PENTING: Mencegah JavaScript membaca cookie (Anti-XSS)
        cookie.setSecure(false);  // Set 'true' jika sudah pakai HTTPS (Production)
        cookie.setPath("/");      // Cookie berlaku di seluruh halaman website
        cookie.setMaxAge(maxAge); 
        
        // Jika token null (logout), value kosong
        if (token == null) {
            cookie.setValue("");
        }

        response.addCookie(cookie);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}