package com.example.processapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "CUSTOMER_INFOS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "FIRST_NAME")
    private String firstName = "";

    @Column(name = "LAST_NAME")
    private String lastName = "";

    @Column(name = "CITY")
    private String city;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "ZIP_CODE")
    private String zipCode;

    @Column(name = "PHONE_NUMBER",unique = true)
    private String phoneNumber;

    @Column(name = "EMAIL",unique = true)
    private String email;

    @Column(name = "IP_ADDRESS")
    private String  ipAddress;

    @Transient
    private boolean isValid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerInfo that = (CustomerInfo) o;
        return Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, email);
    }
}
