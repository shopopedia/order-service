package com.shopopedia.order.service;

import com.shopopedia.order.dto.CreateOrderItemRequest;
import com.shopopedia.order.dto.CreateOrderRequest;
import com.shopopedia.order.dto.OrderItemResponse;
import com.shopopedia.order.dto.OrderResponse;
import com.shopopedia.order.entity.Order;
import com.shopopedia.order.entity.OrderItem;
import com.shopopedia.order.exception.OrderNotFoundException;
import com.shopopedia.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = Order.builder()
                .userId(request.userId())
                .build();

        double totalAmount = 0.0;

        for (CreateOrderItemRequest itemRequest : request.items()) {

            double lineAmount = itemRequest.price() * itemRequest.quantity();
            totalAmount += lineAmount;

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.productId())
                    .quantity(itemRequest.quantity())
                    .price(itemRequest.price())
                    .lineAmount(lineAmount)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getLineAmount()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}