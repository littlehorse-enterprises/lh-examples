package io.littlehorse.examples;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.littlehorse.orderresilience.order.order.dto.CreateOrderRequest;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    private OrderService orderService;
    
    @POST
    public Response createOrder( CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Response.ok(order).build();
    }
    
    @GET
    @Path("/client/{clientId}")
    public Response getOrdersByClientId(@PathParam("clientId") int clientId) {
        List<Order> orders = orderService.getOrdersByClientId(clientId);
        return Response.ok(orders).build();
    }
}
