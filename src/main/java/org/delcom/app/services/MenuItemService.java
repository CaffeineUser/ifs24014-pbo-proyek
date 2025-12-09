package org.delcom.app.services;

import org.delcom.app.entities.Category;
import org.delcom.app.entities.MenuItem;
import org.delcom.app.repositories.CategoryRepository;
import org.delcom.app.repositories.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService; 

    // 1. CREATE & UPDATE (ADMIN FEATURES)

    public MenuItem createMenuItem(MenuItem menuItem, Long categoryId) {
        // Validasi: Cek apakah nama menu sudah ada di kategori yang sama
        if (menuItemRepository.existsByNameAndCategoryId(menuItem.getName(), categoryId)) {
            throw new RuntimeException("Menu dengan nama '" + menuItem.getName() + "' sudah ada di kategori ini.");
        }

        // Set Kategori
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        menuItem.setCategory(category);

        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(Long id, MenuItem updatedData, Long categoryId) {
        MenuItem existingItem = getMenuItemById(id); // Akan throw error jika tidak ketemu

        existingItem.setName(updatedData.getName());
        existingItem.setDescription(updatedData.getDescription());
        existingItem.setPrice(updatedData.getPrice());
        existingItem.setAvailable(updatedData.isAvailable());
        
        // Update URL Gambar jika ada
        if (updatedData.getImageUrl() != null) {
            existingItem.setImageUrl(updatedData.getImageUrl());
        }

        // Update Kategori jika berubah
        if (categoryId != null && !categoryId.equals(existingItem.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Kategori baru tidak ditemukan"));
            existingItem.setCategory(newCategory);
        }

        return menuItemRepository.save(existingItem);
    }

    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Menu tidak ditemukan");
        }
        menuItemRepository.deleteById(id);
    }
    
    // Toggle status tersedia/habis dengan cepat
    public MenuItem toggleAvailability(Long id) {
        MenuItem item = getMenuItemById(id);
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }

    // 2. READ OPERATIONS (CUSTOMER VIEW)

    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu tidak ditemukan dengan ID: " + id));
    }

    public List<MenuItem> getAllAvailableMenu() {
        return menuItemRepository.findByAvailableTrue();
    }

    public List<MenuItem> getMenuByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    /**
     * Fitur Pencarian Lanjutan untuk halaman Menu Utama.
     * Menggunakan Custom Query searchAvailableItems.
     */
    public List<MenuItem> searchMenu(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.searchAvailableItems(keyword, categoryId, minPrice, maxPrice);
    }

    // 3. SPECIAL FEATURES (DASHBOARD/HOME)

    /**
     * Mengambil menu terpopuler berdasarkan jumlah pesanan.
     * Mengonversi List<Object[]> dari repository menjadi List<MenuItem>.
     */
    public List<MenuItem> getPopularMenuItems(int limit) {
        List<Object[]> results = menuItemRepository.findPopularItems();
    
        return results.stream()
                .limit(limit)
                .map(row -> (MenuItem) row[0])
                .collect(Collectors.toList());
    }

    /**
     * Mengambil menu terbaru (New Arrivals)
     */
    public List<MenuItem> getNewArrivals() {
        return menuItemRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    // 4. ADMIN VIEW (ALL ITEMS)
    
    public List<MenuItem> getAllMenuForAdmin() {
        return menuItemRepository.findAll();
    }

    public MenuItem updateMenuImage(Long id, MultipartFile file) {
        MenuItem menuItem = getMenuItemById(id);

        // 1. Hapus file lama jika ada (untuk menghemat penyimpanan)
        if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(menuItem.getImageUrl());
        }

        // 2. Simpan file baru
        // Parameter: (file, EntityID, prefix_nama)
        // Hasil: "menu_12_170999.jpg"
        String filename = fileStorageService.storeFile(file, id, "menu");

        // 3. Update database
        menuItem.setImageUrl(filename);
        return menuItemRepository.save(menuItem);
    }
}