package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean PasswordEncoder ini WAJIB ada karena dipanggil 
     * oleh UserService untuk mengenkripsi password saat Register/Login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Konfigurasi Filter Chain.
     * Karena kita menggunakan AuthInterceptor & AuthController buatan sendiri,
     * kita menonaktifkan fitur bawaan Spring Security agar tidak bentrok.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Matikan CSRF karena kita menggunakan Token JWT (Stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Konfigurasi URL
            .authorizeHttpRequests(auth -> auth
                // PENTING: Kita izinkan SEMUA request (permitAll) di level Spring Security.
                // Kenapa? Karena pengecekan "Siapa yang boleh masuk" (Authorization)
                // sudah ditangani secara detail oleh 'AuthInterceptor' dan anotasi '@RequireRole'.
                .anyRequest().permitAll()
            )

            // 3. Matikan Form Login bawaan Spring (karena kita punya AuthController /api/auth/login)
            .formLogin(AbstractHttpConfigurer::disable)
            
            // 4. Matikan Logout bawaan Spring (karena kita punya AuthController /api/auth/logout)
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}