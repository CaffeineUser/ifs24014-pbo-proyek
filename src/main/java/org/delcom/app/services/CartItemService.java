package org.delcom.app.services;

import org.delcom.app.entities.Cart;
import org.delcom.app.entities.CartItem;
import org.delcom.app.entities.MenuItem;
import org.delcom.app.repositories.CartItemRepository;
import org.delcom.app.repositories.CartRepository;
import org.delcom.app.repositories.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    /**
     * Menambahkan item ke cart. 
     * Jika item sudah ada, quantity akan ditambahkan.
     * Jika belum ada, akan membuat baris baru.
     */
    public CartItem addItemToCart(UUID cartId, Long menuItemId, Integer quantity) {
        // 1. Cek apakah item ini sudah ada di keranjang user tersebut
        return cartItemRepository.findByCartIdAndMenuItemId(cartId, menuItemId)
            .map(existingItem -> {
                // Skenario 1: Item sudah ada -> Update Quantity
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                updateCartTimestamp(cartId); // Update waktu keranjang
                return cartItemRepository.save(existingItem);
            })
            .orElseGet(() -> {
                // Skenario 2: Item belum ada -> Buat Baru
                Cart cart = cartRepository.findById(cartId)
                        .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));
                
                MenuItem menuItem = menuItemRepository.findById(menuItemId)
                        .orElseThrow(() -> new RuntimeException("Menu Item not found with ID: " + menuItemId));

                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setMenuItem(menuItem);
                newItem.setQuantity(quantity);
                
                updateCartTimestamp(cartId); // Update waktu keranjang
                return cartItemRepository.save(newItem);
            });
    }

    /**
     * Mengupdate jumlah item secara spesifik (misal: tombol + atau - di UI).
     * Jika quantity diset ke 0 atau kurang, item otomatis dihapus.
     */
    public void updateItemQuantity(UUID cartId, Long menuItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            removeItemFromCart(cartId, menuItemId);
            return;
        }

        CartItem item = cartItemRepository.findByCartIdAndMenuItemId(cartId, menuItemId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        updateCartTimestamp(cartId);
    }

    /**
     * Menghapus satu jenis menu dari keranjang.
     */
    public void removeItemFromCart(UUID cartId, Long menuItemId) {
        cartItemRepository.deleteByCartIdAndMenuItemId(cartId, menuItemId);
        updateCartTimestamp(cartId);
    }

    /**
     * Mengosongkan seluruh keranjang.
     */
    public void clearCart(UUID cartId) {
        cartItemRepository.deleteByCartId(cartId);
        updateCartTimestamp(cartId);
    }

    /**
     * Mendapatkan semua item dalam keranjang tertentu.
     */
    public List<CartItem> getItemsByCartId(UUID cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    /**
     * Menghitung total jumlah barang (bukan jenis) di keranjang.
     * Contoh: 2 Nasi Goreng + 1 Es Teh = 3 items.
     */
    public Integer getTotalQuantity(UUID cartId) {
        return cartItemRepository.getTotalQuantityInCart(cartId);
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Memperbarui field lastUpdated pada entity Cart setiap kali ada perubahan item.
     * Ini penting agar kita tahu kapan terakhir kali user mengubah keranjangnya.
     */
    private void updateCartTimestamp(UUID cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            cart.updateTimestamp(); // Method helper yang ada di Entity Cart Anda
            cartRepository.save(cart);
        });
    }
}