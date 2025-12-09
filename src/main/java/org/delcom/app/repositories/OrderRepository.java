package org.delcom.app.repositories;

import org.delcom.app.entities.Order;
import org.delcom.app.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    // Find orders by user
    List<Order> findByUserId(UUID userId);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Find orders by user and status
    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
    
    // Find orders within date range
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent orders (limit)
    List<Order> findTop10ByOrderByOrderDateDesc();
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Custom query: Get orders with items eager loaded
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") UUID orderId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithItems(@Param("userId") UUID userId);
    
    // Custom query: Get daily sales statistics
    @Query("SELECT DATE(o.orderDate) as orderDate, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSales " +
           "FROM Order o " +
           "WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate " +
           "GROUP BY DATE(o.orderDate) " +
           "ORDER BY orderDate DESC")
    List<Object[]> getDailySales(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
    
    // Custom query: Get order statistics
    @Query("SELECT COUNT(o) as totalOrders, " +
           "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedOrders, " +
           "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledOrders, " +
           "COALESCE(SUM(o.totalAmount), 0) as totalRevenue " +
           "FROM Order o " +
           "WHERE o.orderDate >= :startDate")
    Object[] getOrderStatistics(@Param("startDate") LocalDateTime startDate);
    
    // Custom query: Get popular delivery addresses
    @Query("SELECT o.deliveryAddress, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.deliveryAddress " +
           "ORDER BY orderCount DESC")
    List<Object[]> getPopularDeliveryAddresses();
    
    // Count orders by status
    long countByStatus(OrderStatus status);
    
    // Get total revenue
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    // Get today's orders count
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE")
    long countTodayOrders();
    
    // Search orders with multiple criteria (for admin)
    @Query("SELECT o FROM Order o WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:customerName IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:customerPhone IS NULL OR o.customerPhone LIKE %:customerPhone%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate)")
    List<Order> searchOrders(@Param("orderNumber") String orderNumber,
                            @Param("customerName") String customerName,
                            @Param("customerPhone") String customerPhone,
                            @Param("status") OrderStatus status,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
}