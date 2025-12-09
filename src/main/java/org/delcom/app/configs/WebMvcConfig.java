package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // 1. KONFIGURASI AKSES FILE STATIC & UPLOADS
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Folder Uploads (Gambar Menu/User)
        Path uploadPath = Paths.get(uploadDir);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");

        // 2. Folder Static Resources (CSS, JS, Images bawaan template)
        // Spring Boot secara default sudah melayani /static/, tapi kita pastikan aman.
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }


    // 2. KONFIGURASI INTERCEPTOR
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // Cek semua URL (Page & API)
                
                .excludePathPatterns(
                        "/",                 // Halaman Home (jika boleh public)
                        "/login",            // Halaman Login
                        "/register",         // Halaman Register
                        "/api/auth/**",      // API Login/Register
                        "/uploads/**",       // Gambar Upload
                        "/css/**",           // File CSS Thymeleaf
                        "/js/**",            // File JS Thymeleaf
                        "/images/**",        // Logo/Icon website
                        "/webjars/**",       // Library frontend (jika pakai bootstrap via maven)
                        "/error"             // Halaman Error default
                ); 
    }
}