package com.shopopedia.order.service;

import com.shopopedia.order.dto.CreateOrderRequest;
import com.shopopedia.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByUserId(Long userId);
}