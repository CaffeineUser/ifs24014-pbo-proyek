package org.delcom.app.repositories;

import org.delcom.app.entities.User;
import org.delcom.app.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchUsers(@Param("name") String name,
                          @Param("email") String email,
                          @Param("role") UserRole role,
                          @Param("enabled") Boolean enabled);
    
    // FIX: Tidak perlu JOIN ke Orders, cukup cek apakah user punya pesanan
    @Query("SELECT DISTINCT o.user FROM Order o")
    List<User> findUsersWithOrders();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.createdAt >= :startDate")
    long countNewCustomersSince(@Param("startDate") LocalDateTime startDate);
    
    long countByRole(UserRole role);
}