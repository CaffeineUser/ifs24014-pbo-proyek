package org.delcom.app.services;

import org.delcom.app.entities.*;
import org.delcom.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional // Memastikan integritas data (rollback jika ada error)
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Melakukan proses Checkout dari Keranjang User.
     * Mengubah CartItem menjadi OrderItem, menghitung total, dan mengosongkan keranjang.
     */
    public Order checkout(UUID userId, String deliveryAddress, String phoneNumber, String notes, String customerName) {
        // 1. Ambil Keranjang User
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Keranjang tidak ditemukan untuk user ini"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Keranjang belanja kosong, tidak dapat melakukan checkout.");
        }

        // 2. Buat Object Order Baru
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setDeliveryAddress(deliveryAddress);
        order.setCustomerPhone(phoneNumber != null ? phoneNumber : cart.getUser().getPhone());
        order.setCustomerName(customerName != null ? customerName : cart.getUser().getName());
        order.setNotes(notes);
        order.setStatus(OrderStatus.PENDING); // Status awal
        // orderDate dan orderNumber dihandle otomatis oleh @PrePersist di Entity

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Konversi CartItem -> OrderItem
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setQuantity(cartItem.getQuantity());
            
            // PENTING: Snapshot harga saat ini. Jika harga menu berubah besok, 
            // harga di order history tidak boleh ikut berubah.
            BigDecimal currentPrice = cartItem.getMenuItem().getPrice();
            orderItem.setPriceAtOrder(currentPrice);
            
            // Hitung subtotal
            totalAmount = totalAmount.add(currentPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 4. Simpan Order
        Order savedOrder = orderRepository.save(order);

        // 5. Kosongkan Keranjang (Sangat Penting!)
        cart.getItems().clear();
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);

        return savedOrder;
    }


    // 2. ORDER MANAGEMENT


    public Order getOrderById(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));
    }

    public List<Order> getUserOrders(UUID userId) {
        return orderRepository.findByUserIdWithItems(userId);
    }

    public Order updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        
        // Validasi logika status (opsional)
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Tidak dapat mengubah status order yang sudah Selesai atau Dibatalkan.");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void cancelOrder(UUID orderId) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }


    // 3. ADMIN & SEARCH FEATURES

    // Pencarian Multi-kriteria (Untuk halaman Admin Order List)
    public List<Order> searchOrders(String orderNumber, String customerName, String phone, 
                                    OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.searchOrders(
            orderNumber, customerName, phone, status, startDate, endDate
        );
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    

    // 4. STATISTICS & DASHBOARD (MAPPING OBJECT[] KE MAP/DTO)

    /**
     * Mengambil ringkasan dashboard untuk Admin.
     * Menggabungkan beberapa query repository menjadi satu object result.
     */
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Statistik hari ini
        stats.put("todayOrderCount", orderRepository.countTodayOrders());
        stats.put("totalRevenue", orderRepository.getTotalRevenue());

        // Statistik Keseluruhan (Total, Completed, Cancelled, Revenue)
        // Mengambil dari query getOrderStatistics (startDate diset null atau epoch untuk semua waktu)
        Object[] generalStats = orderRepository.getOrderStatistics(LocalDateTime.of(2000, 1, 1, 0, 0));
        if (generalStats != null && generalStats.length > 0) {
            Object[] data = (Object[]) generalStats[0]; // JPA mengembalikan List<Object[]>, kita ambil row pertama
            stats.put("totalOrdersAllTime", data[0]);
            stats.put("completedOrders", data[1]);
            stats.put("cancelledOrders", data[2]);
            // data[3] sudah ada di getTotalRevenue, tapi bisa diambil dari sini juga
        }

        return stats;
    }

    /**
     * Mengambil data penjualan harian untuk grafik.
     * Mengubah List<Object[]> menjadi List<Map> agar mudah dibaca JSON frontend.
     */
    public List<Map<String, Object>> getDailySalesChart(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rawData = orderRepository.getDailySales(start, end);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> dailyStat = new HashMap<>();
            dailyStat.put("date", row[0]);        // Tanggal
            dailyStat.put("count", row[1]);       // Jumlah Order
            dailyStat.put("sales", row[2]);       // Total Penjualan (Uang)
            result.add(dailyStat);
        }
        return result;
    }

    /**
     * Mengambil lokasi pengiriman terpopuler.
     */
    public List<Map<String, Object>> getPopularLocations() {
        List<Object[]> rawData = orderRepository.getPopularDeliveryAddresses();
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Ambil top 5 saja misalnya
        int limit = Math.min(rawData.size(), 5);
        for (int i = 0; i < limit; i++) {
            Object[] row = rawData.get(i);
            Map<String, Object> loc = new HashMap<>();
            loc.put("address", row[0]);
            loc.put("count", row[1]);
            result.add(loc);
        }
        return result;
    }
}