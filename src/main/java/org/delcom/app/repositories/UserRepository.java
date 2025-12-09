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
    
    // Find by email
    Optional<User> findByEmail(String email);

     // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by role
    List<User> findByRole(UserRole role);

    // Count users by role
    long countByRole(UserRole role);
    
    // Find users by name (case insensitive search)
    List<User> findByNameContainingIgnoreCase(String name);

    // Find users created after specific date
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // Find enabled/disabled users
    List<User> findByEnabledTrue();
    List<User> findByEnabledFalse();
    
    // Custom query: Find active users by role
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true ORDER BY u.createdAt DESC")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    // Custom query: Count new customers in date range
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.createdAt >= :start")
    long countNewCustomersSince(@Param("start") LocalDateTime start);

    // Custom query: Find users with orders
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o WHERE u.role = 'CUSTOMER'")
    List<User> findUsersWithOrders();

    // Custom query: Search users with multiple criteria
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchUsers(@Param("name") String name,
                          @Param("email") String email,
                          @Param("role") UserRole role,
                          @Param("enabled") Boolean enabled);
}