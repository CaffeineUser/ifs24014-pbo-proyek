package org.delcom.app.services;

import org.delcom.app.entities.Category;
import org.delcom.app.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. BASIC CRUD (ADMIN)    

    public List<Category> getAllCategories() {
        // Mengambil semua kategori diurutkan berdasarkan nama (A-Z)
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan dengan ID: " + id));
    }

    public Category createCategory(Category category) {
        // Validasi: Nama kategori harus unik
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Kategori dengan nama '" + category.getName() + "' sudah ada.");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedData) {
        Category existingCategory = getCategoryById(id);

        // Validasi: Jika nama berubah, cek apakah nama baru sudah dipakai kategori lain
        if (!existingCategory.getName().equalsIgnoreCase(updatedData.getName())) {
            if (categoryRepository.existsByName(updatedData.getName())) {
                throw new RuntimeException("Nama kategori '" + updatedData.getName() + "' sudah digunakan.");
            }
        }

        existingCategory.setName(updatedData.getName());
        existingCategory.setDescription(updatedData.getDescription());
        
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        // Note: Pastikan di Database logic (Constraint) atau di MenuItemService 
        // tidak ada menu yang tergantung pada kategori ini sebelum dihapus,
        // atau gunakan Soft Delete jika perlu.
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Kategori tidak ditemukan");
        }
        categoryRepository.deleteById(id);
    }

    // 2. PUBLIC / CUSTOMER VIEW

    /**
     * Mengambil kategori yang HANYA memiliki menu aktif.
     * Berguna untuk tampilan Customer agar tidak menampilkan kategori kosong.
     */
    public List<Category> getActiveCategoriesForCustomer() {
        return categoryRepository.findCategoriesWithAvailableItems();
    }

    // 3. STATISTICS & REPORTING

    public List<Map<String, Object>> getCategoriesWithItemCounts() {
        List<Object[]> results = categoryRepository.findCategoriesWithItemCount();
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Object[] row : results) {
            Category cat = (Category) row[0];
            Long count = (Long) row[1];

            Map<String, Object> statItem = new HashMap<>();
            statItem.put("id", cat.getId());
            statItem.put("name", cat.getName());
            statItem.put("description", cat.getDescription());
            statItem.put("activeItemCount", count);
            
            stats.add(statItem);
        }
        return stats;
    }

    /**
     * Pencarian Kategori (untuk Admin saat mengelola banyak kategori)
     */
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword);
    }
}