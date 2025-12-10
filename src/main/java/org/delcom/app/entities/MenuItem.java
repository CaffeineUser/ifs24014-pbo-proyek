package org.delcom.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nama menu wajib diisi")
    private String name;
    
    @NotBlank(message = "Deskripsi wajib diisi")
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "Harga wajib diisi")
    @DecimalMin(value = "0.01", message = "Harga minimal 0.01")
    private BigDecimal price;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("menuItems")
    private Category category;
    
    private String imageUrl;
    
    private boolean available = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 1. WAJIB ADA: CONSTRUCTOR KOSONG (NO-ARG CONSTRUCTOR)
    
    public MenuItem() {
        // Hibernate butuh ini untuk membuat object dari database
    }

    // Constructor lengkap (Opsional, tapi bagus untuk testing)
    public MenuItem(String name, String description, BigDecimal price, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = true;
    }

    // 2. LIFECYCLE CALLBACKS
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // =========================================================
    // 3. GETTER & SETTER
    // =========================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}