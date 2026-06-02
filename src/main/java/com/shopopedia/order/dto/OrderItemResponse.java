package com.shopopedia.order.dto;

public record OrderItemResponse(
        Long id,
        Long productId,
        Integer quantity,
        Double price,
        Double lineAmount
) {
}