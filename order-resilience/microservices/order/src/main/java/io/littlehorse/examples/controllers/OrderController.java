package io.littlehorse.examples.controllers;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    OrderService orderService;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> placeOrder(OrderRequest request) throws JsonProcessingException {
        return orderService.runOrderWorkflow(request)
                .onItem().transform(result -> {
                    System.out.println("Result " + result);
                    OrderResponse orderResponse = null;
                    try {
                        orderResponse = objectMapper.readValue((String) result, OrderResponse.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if ("COMPLETED".equals(orderResponse.getStatus())) {
                        return Response.ok(orderResponse).build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(orderResponse.getMessage())
                                .build();
                    }
                })
                .onFailure().recoverWithItem(failure ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Failed to place order: " + failure.getMessage())
                                .build()
                );
    }

    @GET
    @Path("/client/{clientId}")
    public Response getOrdersByClientId(@PathParam("clientId") int clientId) {
        List<OrderResponse> orders = orderService.getOrderResponsesByClientId(clientId);
        return Response.ok(orders).build();
    }
}
