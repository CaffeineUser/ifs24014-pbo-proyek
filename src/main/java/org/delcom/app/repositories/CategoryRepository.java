package org.delcom.app.repositories;

import org.delcom.app.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find category by name
    Category findByName(String name);
    
    // Find categories by name containing (search)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Find all categories ordered by name
    List<Category> findAllByOrderByNameAsc();
    
    // Custom query: Get categories with menu items count
    @Query("SELECT c, COUNT(m) FROM Category c LEFT JOIN c.menuItems m WHERE m.available = true GROUP BY c")
    List<Object[]> findCategoriesWithItemCount();
    
    // Check if category exists by name
    boolean existsByName(String name);
    
    // Find categories with available menu items
    @Query("SELECT DISTINCT c FROM Category c JOIN c.menuItems m WHERE m.available = true")
    List<Category> findCategoriesWithAvailableItems();
}
