package org.delcom.app.services;

import org.delcom.app.entities.MenuItem;
import org.delcom.app.entities.OrderItem;
import org.delcom.app.repositories.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Mayoritas operasi di sini adalah Read (untuk laporan)
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    // 1. ORDER DETAILS (Untuk Receipt/Invoice)

    /**
     * Mengambil semua item dalam satu pesanan.
     * Menggunakan fetch join (findByOrderIdWithMenuItem) untuk performa
     * agar tidak terjadi query N+1 saat mengambil detail menu.
     */
    public List<OrderItem> getItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrderIdWithMenuItem(orderId);
    }

    // 2. DASHBOARD ANALYTICS (ADMIN)

    /**
     * Laporan Menu Terlaris (Top Selling).
     * Mengubah data mentah Object[] dari repository menjadi List<Map> yang rapi.
     * 
     * @param limit Batas jumlah data yang diambil (misal: Top 5)
     */
    public List<Map<String, Object>> getTopSellingItems(int limit) {
        List<Object[]> rawData = orderItemRepository.getTopSellingItems();
        
        // Menggunakan Stream API untuk membatasi jumlah dan mapping data
        return rawData.stream()
                .limit(limit)
                .map(row -> {
                    MenuItem item = (MenuItem) row[0];
                    Long qty = (Long) row[1];
                    
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("menuId", item.getId());
                    stat.put("menuName", item.getName());
                    stat.put("category", item.getCategory().getName());
                    stat.put("totalSold", qty);
                    stat.put("price", item.getPrice()); // Harga saat ini
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * Laporan Penjualan berdasarkan Kategori.
     * Berguna untuk diagram lingkaran (Pie Chart) di Dashboard.
     * Menampilkan: Nama Kategori, Total Item Terjual, Total Pendapatan.
     */
    public List<Map<String, Object>> getSalesByCategoryStats() {
        List<Object[]> rawData = orderItemRepository.getSalesByCategory();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            String categoryName = (String) row[0];
            Long totalQty = (Long) row[1];
            BigDecimal totalSales = (BigDecimal) row[2];

            Map<String, Object> stat = new HashMap<>();
            stat.put("category", categoryName);
            stat.put("totalItemsSold", totalQty);
            stat.put("totalRevenue", totalSales);
            
            result.add(stat);
        }
        return result;
    }
    
    // 3. VALIDATION / UTILS

    /**
     * Mengecek seberapa sering sebuah menu dipesan.
     * Berguna sebelum Admin menghapus menu. Jika count > 0,
     * sebaiknya menu jangan dihapus (hard delete), tapi di-soft delete (available=false),
     * agar riwayat pesanan tidak rusak.
     */
    public boolean isMenuItemHasOrders(Long menuItemId) {
        return orderItemRepository.countByMenuItemId(menuItemId) > 0;
    }

    public long getMenuItemOrderCount(Long menuItemId) {
        return orderItemRepository.countByMenuItemId(menuItemId);
    }
}