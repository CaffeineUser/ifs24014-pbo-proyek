package org.delcom.app.repositories;

import org.delcom.app.entities.Order;
import org.delcom.app.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByUserId(UUID userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findTop10ByOrderByOrderDateDesc();
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") UUID orderId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithItems(@Param("userId") UUID userId);
    
    // ==========================================================
    // PERBAIKAN: Gunakan Native Query untuk Fungsi Tanggal (SQL)
    // ==========================================================
    
    @Query(value = "SELECT CAST(order_date AS DATE) as orderDate, COUNT(*) as orderCount, SUM(total_amount) as totalSales " +
       "FROM orders " +
       "WHERE order_date >= :startDate AND order_date <= :endDate " +
       "AND status = 'COMPLETED' " + 
       "GROUP BY CAST(order_date AS DATE) " +
       "ORDER BY orderDate DESC", nativeQuery = true)
List<Object[]> getDailySales(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT COUNT(*) as totalOrders, " +
           "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedOrders, " +
           "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledOrders, " +
           "COALESCE(SUM(total_amount), 0) as totalRevenue " +
           "FROM orders " +
           "WHERE order_date >= :startDate", nativeQuery = true)
    Object[] getOrderStatistics(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o.deliveryAddress, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.deliveryAddress " +
           "ORDER BY orderCount DESC")
    List<Object[]> getPopularDeliveryAddresses();
    
    long countByStatus(OrderStatus status);
    
    // FIX: Gunakan Native Query agar aman
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED'", nativeQuery = true)
    BigDecimal getTotalRevenue();
    
    // FIX UTAMA: Error "DATE() function" diselesaikan dengan Native Query Postgres
    @Query(value = "SELECT COUNT(*) FROM orders WHERE CAST(order_date AS DATE) = CURRENT_DATE", nativeQuery = true)
    long countTodayOrders();
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:customerName IS NULL OR o.customerName LIKE %:customerName%) AND " +
           "(:customerPhone IS NULL OR o.customerPhone LIKE %:customerPhone%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(o.orderDate >= :startDate) AND " +  // Langsung bandingkan
           "(o.orderDate <= :endDate)")          // Langsung bandingkan
    List<Order> searchOrders(@Param("orderNumber") String orderNumber,
                            @Param("customerName") String customerName,
                            @Param("customerPhone") String customerPhone,
                            @Param("status") OrderStatus status,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
}