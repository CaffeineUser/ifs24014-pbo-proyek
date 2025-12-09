package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    /**
     * Menyimpan token baru ke database saat user berhasil Login.
     */
    public AuthToken saveUserToken(UUID userId, String token) {
        // Karena di entity Anda menggunakan constructor AuthToken(userId, token)
        AuthToken authToken = new AuthToken(userId, token);
        return authTokenRepository.save(authToken);
    }

    /**
     * Mengecek apakah token valid dan milik user tersebut.
     * Dipanggil oleh AuthInterceptor.
     */
    public AuthToken findUserToken(UUID userId, String token) {
        return authTokenRepository.findByUserIdAndToken(userId, token)
                .orElse(null);
    }

    /**
     * Logout: Menghapus satu token spesifik.
     */
    public void logout(String token) {
        // Hapus prefix "Bearer " jika ada
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authTokenRepository.deleteByToken(token);
    }

    /**
     * Logout All: Menghapus semua token milik user tertentu.
     * Berguna untuk fitur "Keluar dari semua perangkat".
     */
    public void logoutAllDevices(UUID userId) {
        authTokenRepository.deleteByUserId(userId);
    }

    /**
     * Validasi sederhana apakah token ada di database.
     */
    public boolean isTokenValid(String token) {
        return authTokenRepository.existsByToken(token);
    }
}