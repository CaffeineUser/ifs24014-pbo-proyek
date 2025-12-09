package org.delcom.app.dto;

import org.delcom.app.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {
    @NotNull(message = "Status baru harus dipilih")
    private OrderStatus status;

    // Getter & Setter
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}