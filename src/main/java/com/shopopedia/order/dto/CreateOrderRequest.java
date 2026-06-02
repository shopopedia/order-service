package com.shopopedia.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "User id is required")
        Long userId,

        @NotEmpty(message = "Order items are required")
        List<@Valid CreateOrderItemRequest> items
) {
}