package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.annotations.RequireRole;
import org.delcom.app.dto.CategoryRequest;
import org.delcom.app.entities.Category;
import org.delcom.app.entities.UserRole;
import org.delcom.app.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // =================================================================
    // PUBLIC ENDPOINTS (Bisa diakses tanpa login / Customer)
    // =================================================================

    /**
     * Mengambil semua kategori (untuk dropdown filter menu).
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Mengambil kategori spesifik by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // ADMIN ONLY ENDPOINTS (Dilindungi oleh @RequireRole)

    /**
     * Tambah Kategori Baru.
     */
    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        Category savedCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(savedCategory);
    }

    /**
     * Update Kategori.
     */
    @PutMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category categoryData = new Category();
        categoryData.setName(request.getName());
        categoryData.setDescription(request.getDescription());
        
        Category updatedCategory = categoryService.updateCategory(id, categoryData);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Hapus Kategori.
     */
    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Kategori berhasil dihapus");
    }

    /**
     * Statistik Kategori (Jumlah item per kategori).
     * Berguna untuk Dashboard Admin.
     */
    @GetMapping("/stats")
    @RequireRole({UserRole.ADMIN, UserRole.STAFF})
    public ResponseEntity<List<Map<String, Object>>> getCategoryStats() {
        return ResponseEntity.ok(categoryService.getCategoriesWithItemCounts());
    }
}