package org.delcom.app.repositories;

import org.delcom.app.entities.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    // Mencari token spesifik (digunakan untuk validasi login)
    Optional<AuthToken> findByToken(String token);

    // Mencari kombinasi user dan token (Validasi ganda)
    Optional<AuthToken> findByUserIdAndToken(UUID userId, String token);

    // Menghapus token (Logout)
    @Modifying
    @Transactional
    void deleteByToken(String token);

    // Menghapus semua token milik user (Logout All Devices)
    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);
    
    // Cek apakah token ada
    boolean existsByToken(String token);
}