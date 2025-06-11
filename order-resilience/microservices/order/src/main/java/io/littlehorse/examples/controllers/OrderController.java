package io.littlehorse.examples.controllers;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.services.OrderService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    private OrderService orderService;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> placeOrder(OrderRequest request) throws JsonProcessingException {
        return orderService.runOrderWorkflow(request);
    }
    
    @GET
    @Path("/client/{clientId}")
    public Response getOrdersByClientId(@PathParam("clientId") int clientId) {
        List<OrderResponse> orders = orderService.getOrderResponsesByClientId(clientId);
        return Response.ok(orders).build();
    }
}
