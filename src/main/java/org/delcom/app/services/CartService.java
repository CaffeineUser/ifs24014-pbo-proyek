package org.delcom.app.services;

import org.delcom.app.entities.*;
import org.delcom.app.repositories.CartRepository;
import org.delcom.app.repositories.MenuItemRepository;
import org.delcom.app.repositories.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get cart for user (create if not exists)
     */
    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }
    
    /**
     * Add item to cart
     */
    public void addItem(UUID userId, Long menuItemId, Integer quantity) {
        Cart cart = getCart(userId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu tidak ditemukan"));
        
        // Check if item already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setMenuItem(menuItem);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
        
        // Update cart timestamp
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);
    }
    
    /**
     * Update item quantity
     */
    public void updateItem(UUID userId, Long menuItemId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(userId, menuItemId);
            return;
        }
        
        Cart cart = getCart(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan di keranjang"));
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);
    }
    
    /**
     * Remove item from cart
     */
    public void removeItem(UUID userId, Long menuItemId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartIdAndMenuItemId(cart.getId(), menuItemId);
        
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);
    }
    
    /**
     * Clear cart
     */
    public void clearCart(UUID userId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);
    }
    
    /**
     * Get cart total
     */
    public BigDecimal getTotal(UUID userId) {
        Cart cart = getCart(userId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : cartItemRepository.findByCartId(cart.getId())) {
            BigDecimal itemPrice = item.getMenuItem().getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        
        return total;
    }
    
    /**
     * Get cart item count
     */
    public int getItemCount(UUID userId) {
    Cart cart = getCart(userId);
    Integer count = cartItemRepository.getTotalQuantityInCart(cart.getId());
    // Jika null (cart kosong), kembalikan 0
    return (count != null) ? count : 0;
}
    
    /**
     * Check if cart is empty
     */
    public boolean isEmpty(UUID userId) {
        Cart cart = getCart(userId);
        return cartItemRepository.countByCartId(cart.getId()) == 0;
    }
    
    /**
     * Get cart with items
     */
    public Optional<Cart> getCartWithItems(UUID userId) {
        return cartRepository.findByUserIdWithItems(userId);
    }
    
    // Private helper method
    private Cart createCart(UUID userId) {
        User user = userService.getUserById(userId);
        
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setLastUpdated(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }
}