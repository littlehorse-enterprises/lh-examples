package io.littlehorse.examples.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        List<String> discountCodes = orderRequest.getDiscountCodes();
        String discountCodesString = String.join(",", discountCodes != null ? discountCodes : List.of());

        Order order = Order.builder()
                .clientId(orderRequest.getClientId())
                .orderLines(new ArrayList<>())
                .discountCodes(discountCodesString)
                .build();

        if (orderRequest.getOrderLines() != null) {
            orderRequest.getOrderLines().forEach(lineRequest -> {
                OrderLine orderLine = new OrderLine();
                orderLine.setProductId(lineRequest.getProductId());
                orderLine.setQuantity(lineRequest.getQuantity());
                order.addOrderLine(orderLine);
            });
        }
        return order;
    }
    

    public OrderResponse toResponse(Order order) {
        OrderResponse response =
        OrderResponse.builder()
                .orderId(order.getOrderId())
                .clientId(order.getClientId())
                .status(order.getStatus())
                .message(order.getMessage())
                .total(order.getTotal())
                .creationDate(order.getCreationDate())
                .discountCodes(
                    order.getDiscountCodes() != null ?
                    Arrays.asList(order.getDiscountCodes().split(",")) :
                    null
                )
                .build();

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
     * Converts an OrderLine entity to an OrderResponse.OrderLineRequest DTO
     * @param orderLine The OrderLine entity
     * @return The OrderLineRequest DTO for OrderResponse
     */
    private OrderResponse.OrderLineResponse toOrderLineRequest(OrderLine orderLine) {
        OrderResponse.OrderLineResponse lineRequest = new OrderResponse.OrderLineResponse();
        lineRequest.setProductId(orderLine.getProductId());
        lineRequest.setQuantity(orderLine.getQuantity());
        lineRequest.setUnitPrice(orderLine.getUnitPrice());
        lineRequest.setDiscountPercentage(orderLine.getDiscountPercentage());
        lineRequest.setTotalPrice(orderLine.getTotalPrice());
        return lineRequest;
    }
}