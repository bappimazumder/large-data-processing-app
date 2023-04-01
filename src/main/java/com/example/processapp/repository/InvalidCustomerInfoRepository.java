package com.example.processapp.repository;

import com.example.processapp.entity.InvalidCustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("invalidCustomerInfoRepository")
public interface InvalidCustomerInfoRepository extends JpaRepository<InvalidCustomerInfo, Long> {
    InvalidCustomerInfo findByEmail(String email);

}
