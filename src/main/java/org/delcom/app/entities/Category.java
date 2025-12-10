package org.delcom.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList; // Import ini
import java.util.List;      // Import ini

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nama kategori wajib diisi")
    private String name;
    
    private String description;
    
    // ==========================================================
    // TAMBAHKAN BAGIAN INI (RELASI KE MENU ITEM)
    // ==========================================================
    // mappedBy = "category" harus sesuai dengan nama field di MenuItem.java
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems = new ArrayList<>();

    // ==========================================================
    
    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // GETTER SETTER BARU UNTUK LIST MENU
    public List<MenuItem> getMenuItems() { return menuItems; }
    public void setMenuItems(List<MenuItem> menuItems) { this.menuItems = menuItems; }
}