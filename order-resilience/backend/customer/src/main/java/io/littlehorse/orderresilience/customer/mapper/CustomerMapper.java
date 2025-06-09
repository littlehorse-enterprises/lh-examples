package io.littlehorse.orderresilience.customer.mapper;

import org.springframework.stereotype.Component;
import io.littlehorse.orderresilience.customer.dto.CustomerRequest;
import io.littlehorse.orderresilience.customer.dto.CustomerResponse;
import io.littlehorse.orderresilience.customer.model.Customer;
import java.util.List;
import java.util.stream.Collectors;

@Component
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
                customer.getEmail()
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
