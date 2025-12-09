package org.delcom.app.repositories;

import org.delcom.app.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find order items by order ID
    List<OrderItem> findByOrderId(UUID orderId);
    
    // Find order items by menu item ID
    List<OrderItem> findByMenuItemId(Long menuItemId);
    
    // Custom query: Get top selling items
    @Query("SELECT oi.menuItem, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY oi.menuItem " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> getTopSellingItems();
    
    // Custom query: Get sales by category
    @Query("SELECT m.category.name, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.priceAtOrder) as totalSales " +
           "FROM OrderItem oi " +
           "JOIN oi.menuItem m " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY m.category.name " +
           "ORDER BY totalSales DESC")
    List<Object[]> getSalesByCategory();
    
    // Custom query: Get order items with menu item details
    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.menuItem WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithMenuItem(@Param("orderId") UUID orderId);
    
    // Count how many times a menu item has been ordered
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId")
    long countByMenuItemId(@Param("menuItemId") Long menuItemId);
}