package org.delcom.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "pages/auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "pages/auth/register";
    }

    // === TAMBAHAN BARU ===
    
    @GetMapping("/cart")
    public String cartPage() {
        return "cart"; // Halaman Keranjang & Checkout
    }

    @GetMapping("/my-orders")
    public String myOrdersPage() {
        return "my-orders"; // Halaman Riwayat Pesanan Customer
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin"; // Halaman Dashboard Admin (Manage Menu & Order)
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile"; // Pastikan buat file profile.html di templates
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403"; // Buat file templates/error/403.html (opsional)
    }
}