package io.littlehorse.examples.controllers;

import java.util.Date;
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
import org.jboss.logging.Logger;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    OrderService orderService;

    @Inject
    ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(OrderController.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> placeOrder(OrderRequest request) throws JsonProcessingException {
        var startDate = new Date();
        LOG.infof("Starting wfRun (order-workflow) for client %d at %s", request.getClientId(), startDate);
        return orderService.runOrderWorkflow(request)
                .onItem().transform(result -> {
                    var endDate = new Date();
                    LOG.infof("Completed wfRun (order-workflow) for client %d at %s, took %d seconds",
                            request.getClientId(), endDate, (endDate.getTime() - startDate.getTime()) / 1000);
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
                .onFailure().recoverWithItem(failure -> {
                            var endDate = new Date();
                            LOG.errorf("Failed wfRun (order-workflow) for client %d at %s, took %d seconds: %s",
                                    request.getClientId(), endDate, (endDate.getTime() - startDate.getTime()) / 1000, failure.getMessage());
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity("Failed to place order: " + failure.getMessage())
                                    .build();
                        }
                );
    }

    @GET
    @Path("/client/{clientId}")
    public Response getOrdersByClientId(@PathParam("clientId") int clientId) {
        List<OrderResponse> orders = orderService.getOrderResponsesByClientId(clientId);
        return Response.ok(orders).build();
    }
}
