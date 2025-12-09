package org.delcom.app.interceptors;

import org.delcom.app.annotations.RequireRole;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthContext authContext;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. Lewati pengecekan untuk file statis (CSS/JS/Gambar) dan Halaman Public
        if (isPublicResource(request)) {
            return true;
        }

        // 2. Ambil Token (Cek Header DULU, kalau kosong cek COOKIE)
        String token = extractToken(request);

        // 3. Validasi Token
        if (token == null || !JwtUtil.validateToken(token, true)) {
            handleUnauthorized(request, response, "Silakan login terlebih dahulu");
            return false;
        }

        UUID userId = JwtUtil.extractUserId(token);
        AuthToken authToken = (userId != null) ? authService.findUserToken(userId, token) : null;

        if (authToken == null) {
            handleUnauthorized(request, response, "Sesi berakhir");
            return false;
        }

        User authUser = userService.getUserById(authToken.getUserId());
        if (authUser == null || !authUser.isEnabled()) {
            handleUnauthorized(request, response, "User tidak aktif");
            return false;
        }

        // 4. Simpan User ke Context
        authContext.setAuthUser(authUser);

        // 5. Cek Role (Jika ada @RequireRole di Controller)
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole roleAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
            
            if (roleAnnotation == null) {
                roleAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            if (roleAnnotation != null) {
                boolean isAllowed = Arrays.stream(roleAnnotation.value())
                        .anyMatch(role -> role == authUser.getRole());

                if (!isAllowed) {
                    // Jika role salah, kirim Error 403 (Forbidden)
                    if (isApiRequest(request)) {
                        sendJsonError(response, 403, "Akses ditolak: Role tidak sesuai");
                    } else {
                        response.sendRedirect("/access-denied"); // Buat halaman ini nanti atau redirect ke home
                    }
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        authContext.clear();
    }

    // ================= HELPER METHODS =================

    private String extractToken(HttpServletRequest request) {
        // Coba ambil dari Header (untuk Postman/API)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Coba ambil dari Cookie (untuk Browser/Thymeleaf)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isPublicResource(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Daftar URL yang BEBAS AKSES
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/uploads/") || 
               path.startsWith("/webjars/") || 
               path.startsWith("/api/auth/") || 
               path.equals("/login") || 
               path.equals("/register") || 
               path.equals("/error") ||
               path.equals("/"); // Home page biasanya public
    }

    // Membedakan apakah request dari API atau Browser
    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    // Menangani error: Redirect jika Browser, JSON jika API
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        if (isApiRequest(request)) {
            sendJsonError(response, 401, message);
        } else {
            // Redirect ke halaman login jika user membuka halaman web
            response.sendRedirect("/login?error=auth_required");
        }
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        String json = String.format("{\"status\":%d, \"message\":\"%s\"}", status, message);
        response.getWriter().write(json);
    }
}