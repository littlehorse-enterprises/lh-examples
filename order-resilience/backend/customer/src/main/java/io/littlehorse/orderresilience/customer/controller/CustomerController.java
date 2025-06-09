package io.littlehorse.orderresilience.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.littlehorse.orderresilience.customer.dto.CustomerResponse;
import io.littlehorse.orderresilience.customer.mapper.CustomerMapper;
import io.littlehorse.orderresilience.customer.model.Customer;
import io.littlehorse.orderresilience.customer.service.CustomerService;

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

}
