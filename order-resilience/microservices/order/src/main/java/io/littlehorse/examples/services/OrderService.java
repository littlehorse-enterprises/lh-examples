package io.littlehorse.examples.services;

import java.util.List;

import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.mappers.OrderMapper;
import io.littlehorse.examples.models.Order;
import io.littlehorse.examples.models.OrderLine;
import io.littlehorse.examples.repositories.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;
    
    @Inject
    private OrderMapper orderMapper;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
        
        // Calculate total if not already set
        if (order.getTotal() == 0 && order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            double totalPrice = order.getOrderLines().stream()
                .mapToDouble(OrderLine::getSubtotal)
                .sum();
            order.setTotal(totalPrice);
        }
        
        // Ensure status is set
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        
        orderRepository.persist(order);
        return order;
    }
    
    @Transactional
    public OrderResponse saveOrder(OrderRequest request) {
        Order order = createOrder(request);
        return orderMapper.toResponse(order);
    }
    
    public List<Order> getOrdersByClientId(int clientId) {
        return orderRepository.findByClientId(clientId);
    }
    
    public List<OrderResponse> getOrderResponsesByClientId(int clientId) {
        List<Order> orders = orderRepository.findByClientId(clientId);
        return orders.stream()
            .map(orderMapper::toResponse)
            .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatus, String message) {
        Order order = orderRepository.findById( orderId);
        
        if (order == null) {
            return null;
        }
        
        order.setStatus(newStatus);
        order.setMessage(message);
        orderRepository.persist(order);
        
        return orderMapper.toResponse(order);
    }


}
