package io.littlehorse.examples;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.littlehorse.orderresilience.order.order.dto.CreateOrderRequest;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setClientId(request.getClientId());
        
        double totalPrice = 0;
        
        for (CreateOrderRequest.OrderLineRequest lineRequest : request.getOrderLines()) {
            OrderLine orderLine = new OrderLine();
            orderLine.setProductId(lineRequest.getProductId());
            orderLine.setQuantity(lineRequest.getQuantity());
            orderLine.setUnitPrice(lineRequest.getUnitPrice());
            
            order.addOrderLine(orderLine);
            totalPrice += orderLine.getSubtotal();
        }
        
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.persist(order);
        return order;
    }
    
    public List<Order> getOrdersByClientId(int clientId) {
        return orderRepository.findByClientId(clientId);
    }
}
