package io.littlehorse.examples.services;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.dto.ProductDiscountItem;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.examples.mappers.OrderMapper;
import io.littlehorse.examples.models.Order;
import io.littlehorse.examples.models.OrderLine;
import io.littlehorse.examples.repositories.OrderRepository;
import io.littlehorse.examples.workflows.OrderWorkflow;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseFutureStub;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final LittleHorseFutureStub futureStub;

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    OrderService(OrderRepository orderRepository, OrderMapper orderMapper, LittleHorseFutureStub littleHorseFutureStub) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.futureStub = littleHorseFutureStub;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
        order.setStatus("PENDING");
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
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return null;
        }
        order.setStatus(newStatus);
        order.setMessage(message);
        orderRepository.persist(order);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse finalizeOrder(Long orderId, ProductPriceItem[] productPrices) throws JsonProcessingException {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return null;
        }
        double total = 0.0;
        for (var orderLine : order.getOrderLines()) {
            var productPrice = Arrays.stream(productPrices)
                    .filter(item -> item.getProductId() == orderLine.getProductId())
                    .findFirst()
                    .orElseThrow();
            orderLine.setUnitPrice(productPrice.getUnitPrice());
            orderLine.setDiscountPercentage(productPrice.getDiscountPercentage());
            orderLine.setUnitPrice(orderLine.getUnitPrice());
            orderLine.setTotalPrice(orderLine.getUnitPrice() * orderLine.getQuantity());
            total += orderLine.getTotalPrice();
        }
        order.setTotal(total);
        order.setStatus("COMPLETED");
        order.setMessage("Order completed and dispatched successfully");
        orderRepository.persist(order);
        return orderMapper.toResponse(order);
    }


    public Uni<String> runOrderWorkflow(OrderRequest orderRequest) throws JsonProcessingException {
        String wfRunId = UUID.randomUUID().toString().replace("-", "");

        RunWfRequest request = RunWfRequest.newBuilder()
                .setWfSpecName(OrderWorkflow.ORDER_WORKFLOW)
                .putVariables(OrderWorkflow.ORDER_VARIABLE, LHLibUtil.objToVarVal(orderRequest))
                .setId(wfRunId)
                .build();

        AwaitWorkflowEventRequest awaitEvent = AwaitWorkflowEventRequest.newBuilder()
                .addEventDefIds(
                        WorkflowEventDefId.newBuilder().setName(OrderWorkflow.ORDER_WORKFLOW))
                .setWfRunId(LHLibUtil.wfRunIdFromString(wfRunId))
                .build();

        return Uni.createFrom()
                .future(futureStub.runWf(request))
                .chain(() -> Uni.createFrom().future(futureStub.awaitWorkflowEvent(awaitEvent)).ifNoItem().after(Duration.ofSeconds(10)).fail())
                .map(wfEvent -> wfEvent.getContent().getJsonObj());
    }


}
