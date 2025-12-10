package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.annotations.RequireRole;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.CheckoutRequest;
import org.delcom.app.dto.UpdateOrderStatusRequest;
import org.delcom.app.entities.Order;
import org.delcom.app.entities.OrderStatus;
import org.delcom.app.entities.User;
import org.delcom.app.entities.UserRole;
import org.delcom.app.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthContext authContext;

    // ==========================================
    // CUSTOMER ENDPOINTS
    // ==========================================

    @PostMapping("/checkout")
    @RequireRole(UserRole.CUSTOMER)
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request) {
        User user = authContext.getAuthUser();
        Order order = orderService.checkout(
                user.getId(),
                request.getDeliveryAddress(),
                request.getPhoneNumber(),
                request.getNotes(),
                request.getCustomerName()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @RequireRole(UserRole.CUSTOMER)
    public ResponseEntity<List<Order>> getMyOrders() {
        User user = authContext.getAuthUser();
        return ResponseEntity.ok(orderService.getUserOrders(user.getId()));
    }

    @PostMapping("/{id}/cancel")
    @RequireRole(UserRole.CUSTOMER)
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id) {
        // Tambahkan validasi kepemilikan di Service atau di sini
        // Untuk simpelnya, kita asumsikan user boleh cancel ordernya sendiri
        orderService.cancelOrder(id);
        return ResponseEntity.ok("Pesanan berhasil dibatalkan");
    }

    // ==========================================
    // SHARED ENDPOINTS (Detail Order)
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable UUID id) {
        User user = authContext.getAuthUser();
        Order order = orderService.getOrderById(id);

        // Security check: Customer hanya boleh lihat order sendiri
        if (user.isCustomer() && !order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(order);
    }

    // ==========================================
    // ADMIN / STAFF ENDPOINTS
    // ==========================================

    /**
     * Search & Filter Orders (Untuk Halaman Admin List Order)
     */
    @GetMapping("/admin/search")
    @RequireRole({UserRole.ADMIN, UserRole.STAFF})
    public ResponseEntity<List<Order>> searchOrders(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(orderService.searchOrders(orderNumber, customerName, null, status, startDate, endDate));
    }

    /**
     * Update Status Order (Misal: PENDING -> PREPARING)
     */
    @PutMapping("/{id}/status")
    @RequireRole({UserRole.ADMIN, UserRole.STAFF})
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        
        Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}