package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.entities.UserRole;
import org.delcom.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Pastikan Anda punya SecurityConfig
    
    // =================================================================
    // 1. AUTHENTICATION & REGISTRATION
    // =================================================================

    /**
     * Digunakan oleh AuthController untuk Login.
     * Mengembalikan User jika sukses, throw Exception jika gagal.
     */
    public User authenticate(String email, String password) {
        // 1. Cari User by Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan / belum terdaftar"));

        // 2. Cek apakah akun aktif
        if (!user.isEnabled()) {
            throw new RuntimeException("Akun Anda telah dinonaktifkan. Hubungi admin.");
        }

        // 3. Cocokkan Password (Raw vs Hash)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        return user;
    }
    
    /**
     * Registrasi User Baru (Default: Customer).
     */
    public User registerUser(User user) {
        // 1. Cek Duplikasi Email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email " + user.getEmail() + " sudah digunakan.");
        }

        // 2. Enkripsi Password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 3. Set Default Role & Status
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }
        user.setEnabled(true);
        
        return userRepository.save(user);
    }
    
    /**
     * Registrasi khusus Admin/Staff (Biasanya dipanggil lewat Admin Panel)
     */
    public User createUserWithRole(User user, UserRole role) {
        user.setRole(role);
        return registerUser(user);
    }

    // =================================================================
    // 2. PROFILE MANAGEMENT
    // =================================================================

    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan ID: " + id));
    }

    public User updateProfile(UUID userId, String name, String phone, String address) {
        User user = getUserById(userId);
        
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        
        return userRepository.save(user);
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        // Cek password lama
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Password lama tidak sesuai");
        }

        // Simpan password baru
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // =================================================================
    // 3. ADMIN FEATURES (USER MANAGEMENT)
    // =================================================================
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUserRole(UUID userId, UserRole role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }
    
    public User toggleUserStatus(UUID userId) {
        User user = getUserById(userId);
        user.setEnabled(!user.isEnabled()); // Toggle true/false
        return userRepository.save(user);
    }
    
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User tidak ditemukan");
        }
        userRepository.deleteById(userId);
    }

    // Pencarian User (Admin Dashboard)
    public List<User> searchUsers(String name, String email, UserRole role, Boolean enabled) {
        return userRepository.searchUsers(name, email, role, enabled);
    }

    // =================================================================
    // 4. STATISTICS & ANALYTICS
    // =================================================================
    
    public long countNewCustomers(LocalDateTime startDate) {
        return userRepository.countNewCustomersSince(startDate);
    }
    
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
    
    public List<User> getActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }
}