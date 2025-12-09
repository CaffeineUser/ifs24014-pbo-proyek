package org.delcom.app.repositories;

import org.delcom.app.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find cart item by cart and menu item
    Optional<CartItem> findByCartIdAndMenuItemId(UUID cartId, Long menuItemId);
    
    // Find all items in a cart
    List<CartItem> findByCartId(UUID cartId);
    
    // Delete all items from a cart
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);
    
    // Delete specific item from cart
    @Transactional
    @Modifying
    void deleteByCartIdAndMenuItemId(UUID cartId, Long menuItemId);
    
    // Count items in cart
    long countByCartId(UUID cartId);
    
    // Get total quantity in cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantityInCart(@Param("cartId") UUID cartId);
}