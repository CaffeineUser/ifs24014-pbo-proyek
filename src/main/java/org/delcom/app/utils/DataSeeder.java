package org.delcom.app.utils;

import org.delcom.app.entities.*;
import org.delcom.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Buat User ADMIN (Jika belum ada)
        if (!userRepository.existsByEmail("admin@delcom.com")) {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail("admin@delcom.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setPhone("081234567890");
            admin.setAddress("Kantor Pusat");
            userRepository.save(admin);
            System.out.println(">>> User ADMIN created: admin@delcom.com / admin123");
        }

        // 2. Buat User CUSTOMER (Contoh)
        if (!userRepository.existsByEmail("user@delcom.com")) {
            User user = new User();
            user.setName("John Doe");
            user.setEmail("user@delcom.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(UserRole.CUSTOMER);
            user.setPhone("08987654321");
            user.setAddress("Jl. Delcom No. 10");
            userRepository.save(user);
        }

        // 3. Buat Kategori
        if (categoryRepository.count() == 0) {
            Category makanan = new Category();
            makanan.setName("Makanan Utama");
            makanan.setDescription("Hidangan lezat mengenyangkan");
            categoryRepository.save(makanan);

            Category minuman = new Category();
            minuman.setName("Minuman");
            minuman.setDescription("Pelepas dahaga");
            categoryRepository.save(minuman);

            // 4. Buat Menu Contoh
            MenuItem nasiGoreng = new MenuItem();
            nasiGoreng.setName("Nasi Goreng Spesial");
            nasiGoreng.setDescription("Nasi goreng dengan topping ayam, telur, dan kerupuk.");
            nasiGoreng.setPrice(new BigDecimal("25000"));
            nasiGoreng.setImageUrl("https://images.unsplash.com/photo-1633945274309-2c16c9682a8c?ixlib=rb-1.2.1&auto=format&fit=crop&w=200&h=200&q=80");
            nasiGoreng.setCategory(makanan);
            nasiGoreng.setAvailable(true);
            menuItemRepository.save(nasiGoreng);

            MenuItem esTeh = new MenuItem();
            esTeh.setName("Es Teh Manis");
            esTeh.setDescription("Teh asli dengan gula batu.");
            esTeh.setPrice(new BigDecimal("5000"));
            esTeh.setCategory(minuman);
            esTeh.setImageUrl("https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?ixlib=rb-1.2.1&auto=format&fit=crop&w=400&h=300&q=80");
            esTeh.setAvailable(true);
            menuItemRepository.save(esTeh);
            
            System.out.println(">>> Dummy Data Menu created!");
        }
    }
}