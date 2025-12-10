package org.delcom.app.controllers;

import org.delcom.app.annotations.RequireRole;
import org.delcom.app.entities.UserRole;
import org.delcom.app.services.OrderItemService;
import org.delcom.app.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequireRole({UserRole.ADMIN, UserRole.STAFF}) // Hanya Admin/Staff
public class AdminDashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    /**
     * Ringkasan Header (Total Order, Omset Hari Ini, dll)
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(orderService.getAdminDashboardStats());
    }

    /**
     * Grafik Penjualan Harian
     */
    @GetMapping("/chart/sales")
    public ResponseEntity<List<Map<String, Object>>> getSalesChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(orderService.getDailySalesChart(startDate, endDate));
    }

    /**
     * Menu Terlaris (Top Selling)
     */
    @GetMapping("/top-items")
    public ResponseEntity<List<Map<String, Object>>> getTopItems() {
        return ResponseEntity.ok(orderItemService.getTopSellingItems(5));
    }

    /**
     * Penjualan per Kategori (Pie Chart)
     */
    @GetMapping("/category-sales")
    public ResponseEntity<List<Map<String, Object>>> getCategorySales() {
        return ResponseEntity.ok(orderItemService.getSalesByCategoryStats());
    }
}