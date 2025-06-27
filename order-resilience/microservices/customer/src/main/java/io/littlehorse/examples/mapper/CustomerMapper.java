package io.littlehorse.examples.mapper;

import io.littlehorse.examples.dto.CustomerRequest;
import io.littlehorse.examples.dto.CustomerResponse;
import io.littlehorse.examples.models.Customer;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CustomerMapper {

    public Customer toEntity(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer.setName(customerRequest.getName());
        customer.setEmail(customerRequest.getEmail());
        return customer;
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getDescription(),
                customer.getType()
        );
    }

    public List<CustomerResponse> toResponseList(List<Customer> customers) {
        return customers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateEntityFromRequest(Customer existingCustomer, CustomerRequest customerRequest) {
        existingCustomer.setName(customerRequest.getName());
        existingCustomer.setEmail(customerRequest.getEmail());
    }
}
