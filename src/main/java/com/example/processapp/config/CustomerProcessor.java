package com.example.processapp.config;

import com.example.processapp.dto.CustomerDTO;
import com.example.processapp.entity.CustomerInfo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomerProcessor implements ItemProcessor<CustomerInfo, CustomerDTO> {

    @Override
    public CustomerDTO process(CustomerInfo customerInfo) throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customerInfo.getId());
        customerDTO.setFirstName(customerInfo.getFirstName());
        customerDTO.setLastName(customerInfo.getLastName());
        customerDTO.setEmail(customerInfo.getEmail());
        customerDTO.setPhoneNumber(customerInfo.getPhoneNumber());
        customerDTO.setCity(customerInfo.getCity());
        customerDTO.setCountry(customerInfo.getCountry());
        customerDTO.setZipCode(customerInfo.getZipCode());
        customerDTO.setIpAddress(customerInfo.getIpAddress());
        return customerDTO;
    }
}
