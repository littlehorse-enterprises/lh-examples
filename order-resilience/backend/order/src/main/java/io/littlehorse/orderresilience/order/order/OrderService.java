package io.littlehorse.orderresilience.order.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.littlehorse.orderresilience.order.order.dto.CreateOrderRequest;

@Service
public class OrderService {

    @Autowired
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
        
        return orderRepository.save(order);
    }
    
    public List<Order> getOrdersByClientId(int clientId) {
        return orderRepository.findByClientId(clientId);
    }
}
