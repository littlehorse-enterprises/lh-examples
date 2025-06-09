package io.littlehorse.orderresilience.customer.controllers;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.littlehorse.orderresilience.customer.mapper.CustomerMapper;
import io.littlehorse.orderresilience.customer.models.Customer;
import io.littlehorse.orderresilience.customer.services.CustomerService;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerController {

    @Inject
    CustomerService customerService;
    
    @Inject
    CustomerMapper customerMapper;

    @GET
    public Response getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return Response.ok(customerMapper.toResponseList(customers)).build();
    }

}
