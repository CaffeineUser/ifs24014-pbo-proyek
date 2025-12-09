package org.delcom.app.repositories;

import org.delcom.app.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    // Find cart by user ID
    Optional<Cart> findByUserId(UUID userId);
    
    // Find cart with items eager loaded
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.menuItem " +
           "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);
    
    // Delete cart by user ID
    void deleteByUserId(UUID userId);
    
    // Check if cart exists for user
    boolean existsByUserId(UUID userId);
    
    // Find carts that haven't been updated in a while (for cleanup)
    @Query("SELECT c FROM Cart c WHERE c.lastUpdated < :cutoffDate")
    List<Cart> findAbandonedCarts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}