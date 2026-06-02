package com.shopopedia.order.controller;

import com.shopopedia.order.dto.ApiResponse;
import com.shopopedia.order.dto.CreateOrderRequest;
import com.shopopedia.order.dto.OrderResponse;
import com.shopopedia.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Order created successfully",
                response
        );
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long orderId) {

        OrderResponse response = orderService.getOrderById(orderId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Order fetched successfully",
                response
        );
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {

        List<OrderResponse> response = orderService.getOrdersByUserId(userId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "User orders fetched successfully",
                response
        );
    }
}