package io.littlehorse.examples.mappers;

import java.util.stream.Collectors;

import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.models.Order;
import io.littlehorse.examples.models.OrderLine;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderMapper {

    /**
     * Converts an Order request to an Order entity
     * @param orderRequest The order request DTO
     * @return The Order entity
     */
    public Order toEntity(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderId(orderRequest.getOrderId());
        order.setClientId(orderRequest.getClientId());
        order.setErrorMessage(orderRequest.getErrorMessage());
        order.setTotal(orderRequest.getTotal());

        if (orderRequest.getOrderLines() != null) {
            orderRequest.getOrderLines().forEach(lineRequest -> {
                OrderLine orderLine = new OrderLine();
                orderLine.setProductId(lineRequest.getProductId());
                orderLine.setQuantity(lineRequest.getQuantity());
                orderLine.setUnitPrice(lineRequest.getUnitPrice());
                order.addOrderLine(orderLine);
            });
        }
        
        return order;
    }
    
    /**
     * Converts an Order entity to an Order response
     * @param order The Order entity
     * @return The OrderResponse DTO
     */
    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setClientId(order.getClientId());
        response.setErrorMessage(order.getErrorMessage());
        response.setTotal(order.getTotal());
        response.setStatus(order.getStatus());
        
        if (order.getOrderLines() != null) {
            response.setOrderLines(
                order.getOrderLines().stream()
                    .map(this::toOrderLineRequest)
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
    
    /**
     * Updates an existing Order entity with data from an OrderRequest
     * @param order The existing Order entity to update
     * @param orderRequest The OrderRequest containing new data
     * @return The updated Order entity
     */
    public Order updateEntityFromRequest(Order order, OrderRequest orderRequest) {
        order.setClientId(orderRequest.getClientId());
        order.setErrorMessage(orderRequest.getErrorMessage());
        order.setTotal(orderRequest.getTotal());

        // Clear existing order lines and add new ones
        order.getOrderLines().clear();
        
        if (orderRequest.getOrderLines() != null) {
            orderRequest.getOrderLines().forEach(lineRequest -> {
                OrderLine orderLine = new OrderLine();
                orderLine.setProductId(lineRequest.getProductId());
                orderLine.setQuantity(lineRequest.getQuantity());
                orderLine.setUnitPrice(lineRequest.getUnitPrice());
                order.addOrderLine(orderLine);
            });
        }
        
        return order;
    }
    
    /**
     * Converts an OrderLine entity to an OrderResponse.OrderLineRequest DTO
     * @param orderLine The OrderLine entity
     * @return The OrderLineRequest DTO for OrderResponse
     */
    private OrderResponse.OrderLineRequest toOrderLineRequest(OrderLine orderLine) {
        OrderResponse.OrderLineRequest lineRequest = new OrderResponse.OrderLineRequest();
        lineRequest.setProductId(orderLine.getProductId());
        lineRequest.setQuantity(orderLine.getQuantity());
        lineRequest.setUnitPrice(orderLine.getUnitPrice());
        return lineRequest;
    }
}