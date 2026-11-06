package com.dulcemayor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(String id, String clientName, String status, String paymentMethod,
                               LocalDateTime createdAt, LocalDateTime invoicedAt, BigDecimal total, List<Line> items) {
    public record Line(String productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal total) {}
}
