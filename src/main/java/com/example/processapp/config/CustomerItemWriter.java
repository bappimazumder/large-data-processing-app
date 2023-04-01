package com.example.processapp.config;

import com.example.processapp.entity.CustomerInfo;
import com.example.processapp.entity.InvalidCustomerInfo;
import com.example.processapp.repository.CustomerInfoRepository;
import com.example.processapp.repository.InvalidCustomerInfoRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomerItemWriter implements ItemWriter<CustomerInfo> {

    @Autowired
    public CustomerInfoRepository customerInfoRepository;
    @Autowired
    public InvalidCustomerInfoRepository invalidCustomerInfoRepository;


    @Override
    public void write(List<? extends CustomerInfo> list) throws Exception {

        Set<CustomerInfo> validList = list.stream().filter(l -> l.isValid() == true).collect(Collectors.toSet());
        List<CustomerInfo> invalidList = list.stream().filter(l -> l.isValid() == false).collect(Collectors.toList());
        List<InvalidCustomerInfo> invalidCustomerList = new ArrayList<>();
        customerInfoRepository.saveAll(validList);
        for (CustomerInfo c : invalidList) {
            InvalidCustomerInfo invalidCustomerInfo = new InvalidCustomerInfo();
            BeanUtils.copyProperties(c, invalidCustomerInfo);
            invalidCustomerList.add(invalidCustomerInfo);
        }
        invalidCustomerInfoRepository.saveAll(invalidCustomerList);
    }


}
