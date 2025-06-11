package io.littlehorse.examples.services;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.mappers.OrderMapper;
import io.littlehorse.examples.models.Order;
import io.littlehorse.examples.models.OrderLine;
import io.littlehorse.examples.repositories.OrderRepository;
import io.littlehorse.examples.tasks.OrderTask;
import io.littlehorse.examples.workflows.OrderWorkflow;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseFutureStub;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;
    
    private final OrderMapper orderMapper;

    ObjectMapper objectMapper;

    private final LittleHorseFutureStub futureStub;

   OrderService(OrderRepository orderRepository, OrderMapper orderMapper, ObjectMapper objectMapper, LittleHorseFutureStub littleHorseFutureStub){
    this.orderRepository = orderRepository;
    this.orderMapper = orderMapper;
    this.objectMapper = objectMapper;
    this.futureStub = littleHorseFutureStub;
   }

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


    public Uni<String> runOrderWorkflow(OrderRequest orderRequest) throws JsonProcessingException {
        String wfRunId = UUID.randomUUID().toString().replace("-", "");

        RunWfRequest request = RunWfRequest.newBuilder()
                .setWfSpecName(OrderWorkflow.ORDER_WORKFLOW)
                .putVariables(OrderWorkflow.ORDER_VARIABLE, VariableValue.newBuilder().setJsonObj(objectMapper.writeValueAsString(orderRequest)).build())
                .setId(wfRunId)
                .build();

        AwaitWorkflowEventRequest awaitEvent = AwaitWorkflowEventRequest.newBuilder()
                .addEventDefIds(
                        WorkflowEventDefId.newBuilder().setName(OrderWorkflow.ORDER_WORKFLOW))
                .setWfRunId(LHLibUtil.wfRunIdFromString(wfRunId))
                .build();

        return Uni.createFrom()
                .future(futureStub.runWf(request))
                .chain(() -> Uni.createFrom().future(futureStub.awaitWorkflowEvent(awaitEvent)))
                .map(wfEvent -> wfEvent.getContent().getJsonObj());
    }


}
