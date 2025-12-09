package org.delcom.app.controllers;

import org.delcom.app.annotations.RequireRole;
import org.delcom.app.entities.MenuItem;
import org.delcom.app.entities.UserRole;
import org.delcom.app.services.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    
    // PUBLIC ENDPOINTS
    

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenu() {
        return ResponseEntity.ok(menuItemService.getAllAvailableMenu());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MenuItem>> getMenuByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuItemService.getMenuByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItem>> searchMenu(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(menuItemService.searchMenu(keyword, categoryId, minPrice, maxPrice));
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    /**
     * Create Menu dengan Gambar.
     * Menggunakan MediaType.MULTIPART_FORM_DATA_VALUE karena ada upload file.
     * Data JSON dikirim sebagai String dan diparsing manual atau menggunakan @RequestPart.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<MenuItem> createMenu(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("categoryId") Long categoryId,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 1. Buat object Menu
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(price);
        
        // 2. Simpan Data ke DB
        MenuItem savedMenu = menuItemService.createMenuItem(menuItem, categoryId);

        // 3. Upload Gambar jika ada
        if (image != null && !image.isEmpty()) {
            menuItemService.updateMenuImage(savedMenu.getId(), image);
        }

        return ResponseEntity.ok(savedMenu);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<MenuItem> updateMenu(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("available") boolean available,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 1. Siapkan data update
        MenuItem updateData = new MenuItem();
        updateData.setName(name);
        updateData.setDescription(description);
        updateData.setPrice(price);
        updateData.setAvailable(available);

        // 2. Update Data
        MenuItem updatedMenu = menuItemService.updateMenuItem(id, updateData, categoryId);

        // 3. Update Gambar jika ada file baru
        if (image != null && !image.isEmpty()) {
            updatedMenu = menuItemService.updateMenuImage(id, image);
        }

        return ResponseEntity.ok(updatedMenu);
    }

    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok("Menu berhasil dihapus");
    }
}