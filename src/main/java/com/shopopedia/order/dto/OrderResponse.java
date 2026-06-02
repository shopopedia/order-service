package com.shopopedia.order.dto;

import com.shopopedia.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        Double totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}