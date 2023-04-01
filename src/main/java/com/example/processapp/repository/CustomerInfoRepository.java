package com.example.processapp.repository;


import com.example.processapp.entity.CustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("customerInfoRepository")
public interface CustomerInfoRepository extends JpaRepository<CustomerInfo, Long> {
    CustomerInfo findByEmail(String email);

    CustomerInfo findByPhoneNumber(String phoneNo);



}
