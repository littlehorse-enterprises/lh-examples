package io.littlehorse.examples.controllers;

import java.util.List;

import io.littlehorse.examples.mapper.CustomerMapper;
import io.littlehorse.examples.models.Customer;
import io.littlehorse.examples.services.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


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
