package com.example.processapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CustomerDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String city;
    private String country;
    private String zipCode;
    private String phoneNumber;

    private String email;
    private String ipAddress;

}
