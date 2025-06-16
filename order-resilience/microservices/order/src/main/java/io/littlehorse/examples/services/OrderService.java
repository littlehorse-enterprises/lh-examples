package io.littlehorse.examples.services;

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

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
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

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Enabling output topic");
        this.futureStub.putTenant(PutTenantRequest.newBuilder().setId("default").setOutputTopicConfig(OutputTopicConfig.newBuilder()).build());
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
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
    public OrderResponse finalizeOrder(Long orderId, ProductPriceItem[] productPrices, ProductDiscountItem[] discountItems) throws JsonProcessingException {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return null;
        }
        // Apply discounts to order
        double totalDiscount = 0.0;
        for (var orderLine : order.getOrderLines()) {
            var productPrice = Arrays.stream(productPrices)
                    .filter(item -> item.getProductId() == orderLine.getProductId())
                    .findFirst()
                    .map(ProductPriceItem::getUnitPrice)
                    .orElseThrow();
            orderLine.setUnitPrice(productPrice);
            double discountPercentage = Arrays.stream(discountItems)
                    .filter(item -> item.getProductId() == orderLine.getProductId())
                    .findFirst()
                    .map(ProductDiscountItem::getDiscountPercentage)
                    .orElse(0.0);
            orderLine.setDiscountPercentage(discountPercentage);
            orderLine.setUnitPrice(orderLine.getUnitPrice());
            orderLine.setTotalPrice(orderLine.getUnitPrice() * orderLine.getQuantity() * (1 - discountPercentage / 100));
            totalDiscount += orderLine.getTotalPrice();
        }
        order.setTotal(totalDiscount);
        order.setStatus("COMPLETED");
        order.setMessage("Order finalized and dispatched successfully");
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
                .chain(() -> Uni.createFrom().future(futureStub.awaitWorkflowEvent(awaitEvent)))
                .map(wfEvent -> wfEvent.getContent().getJsonObj());
    }


}
