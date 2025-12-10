package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.annotations.RequireRole;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.AddToCartRequest;
import org.delcom.app.dto.UpdateCartItemRequest;
import org.delcom.app.entities.Cart;
import org.delcom.app.entities.User;
import org.delcom.app.entities.UserRole;
import org.delcom.app.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequireRole(UserRole.CUSTOMER) // Hanya Customer yang punya keranjang belanja
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthContext authContext;

    /**
     * Lihat Keranjang Saya
     */
    @GetMapping
    public ResponseEntity<Cart> getMyCart() {
        User user = authContext.getAuthUser();
        // Menggunakan getCartWithItems agar detail menu ikut terambil
        Cart cart = cartService.getCartWithItems(user.getId())
                .orElseGet(() -> cartService.getCart(user.getId()));
        return ResponseEntity.ok(cart);
    }

    /**
     * Tambah Item ke Keranjang
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@Valid @RequestBody AddToCartRequest request) {
        User user = authContext.getAuthUser();
        cartService.addItem(user.getId(), request.getMenuItemId(), request.getQuantity());
        return ResponseEntity.ok("Item berhasil ditambahkan ke keranjang");
    }

    /**
     * Update Jumlah Item (Misal: dari 1 jadi 2)
     */
    @PutMapping("/items/{menuItemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        User user = authContext.getAuthUser();
        
        cartService.updateItem(user.getId(), menuItemId, request.getQuantity());
        return ResponseEntity.ok("Jumlah item berhasil diupdate");
    }

    /**
     * Hapus Satu Item
     */
    @DeleteMapping("/items/{menuItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long menuItemId) {
        User user = authContext.getAuthUser();
        cartService.removeItem(user.getId(), menuItemId);
        return ResponseEntity.ok("Item dihapus dari keranjang");
    }

    /**
     * Kosongkan Keranjang
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        User user = authContext.getAuthUser();
        cartService.clearCart(user.getId());
        return ResponseEntity.ok("Keranjang dikosongkan");
    }
}