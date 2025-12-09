package org.delcom.app.repositories;

import org.delcom.app.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    // Find all available menu items
    List<MenuItem> findByAvailableTrue();
    
    // Find by category and available
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    
    // Find by category
    List<MenuItem> findByCategoryId(Long categoryId);
    
    // Find by price range
    List<MenuItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Search by name
    List<MenuItem> findByNameContainingIgnoreCase(String name);
    
    // Get featured items (limit)
    List<MenuItem> findTop8ByAvailableTrueOrderByCreatedAtDesc();
    
    // Find items created recently
    List<MenuItem> findTop10ByOrderByCreatedAtDesc();
    
    // Custom query: Search with multiple filters
    @Query("SELECT m FROM MenuItem m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR m.price <= :maxPrice) AND " +
           "m.available = true")
    List<MenuItem> searchAvailableItems(@Param("name") String name,
                                       @Param("categoryId") Long categoryId,
                                       @Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice);
    
    // Custom query: Get popular items (most ordered)
    @Query("SELECT m, COUNT(oi) as orderCount FROM MenuItem m " +
           "LEFT JOIN OrderItem oi ON m.id = oi.menuItem.id " +
           "WHERE m.available = true " +
           "GROUP BY m " +
           "ORDER BY orderCount DESC")
    List<Object[]> findPopularItems();
    
    // Check if menu item exists by name in category
    boolean existsByNameAndCategoryId(String name, Long categoryId);
}
