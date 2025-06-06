package io.littlehorse.orderresilience.customer.customer;

import java.util.List;
import java.util.UUID;

import io.littlehorse.orderresilience.customer.customer.exceptions.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.littlehorse.orderresilience.customer.customer.dto.CustomerRequest;
import io.littlehorse.orderresilience.customer.customer.dto.CustomerResponse;
import io.littlehorse.orderresilience.customer.customer.mapper.CustomerMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return new ResponseEntity<>(customerMapper.toResponseList(customers), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable("id") UUID id) throws CustomerNotFoundException {
        Customer customer = customerService.get(id);
        return new ResponseEntity<>(customerMapper.toResponse(customer), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        Customer customer = customerMapper.toEntity(customerRequest);
        Customer createdCustomer = customerService.create(customer);
        return new ResponseEntity<>(customerMapper.toResponse(createdCustomer), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable("id") UUID id, 
            @Valid @RequestBody CustomerRequest customerRequest) throws CustomerNotFoundException {
        Customer customer = customerMapper.toEntity(customerRequest);
        Customer updatedCustomer = customerService.update(id, customer);
        return new ResponseEntity<>(customerMapper.toResponse(updatedCustomer), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable("id") UUID id) throws CustomerNotFoundException {
        customerService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
