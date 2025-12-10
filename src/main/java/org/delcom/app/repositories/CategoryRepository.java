package org.delcom.app.repositories;

import org.delcom.app.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Category findByName(String name);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    List<Category> findAllByOrderByNameAsc();
    
    // Query ini yang sebelumnya ERROR. Sekarang akan berhasil karena
    // field 'menuItems' sudah ada di Entity Category.
    @Query("SELECT c, COUNT(m) FROM Category c LEFT JOIN c.menuItems m WHERE m.available = true GROUP BY c")
    List<Object[]> findCategoriesWithItemCount();
    
    boolean existsByName(String name);
    
    // Query ini juga akan berhasil sekarang
    @Query("SELECT DISTINCT c FROM Category c JOIN c.menuItems m WHERE m.available = true")
    List<Category> findCategoriesWithAvailableItems();
}