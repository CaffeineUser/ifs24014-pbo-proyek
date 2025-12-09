package org.delcom.app.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {
    @NotBlank(message = "Alamat pengiriman wajib diisi")
    private String deliveryAddress;

    @NotBlank(message = "Nomor telepon wajib diisi")
    private String phoneNumber;

    private String notes;
    private String customerName; // Opsional jika ingin ganti nama penerima

    // Getter & Setter
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}