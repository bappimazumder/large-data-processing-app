package com.example.processapp.config;
/**
 * The class use to process customer table data to map
 * in customer object
 *
 * @version 1.0
 * @author Bappi Mazumder
 * @since 2023-03-31
 */
import com.example.processapp.entity.CustomerInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CustomerInfoDBRowMapper implements RowMapper<CustomerInfo> {

    /**
     * This method helps to map the result set into customer object
     *
     * @return  this return a customer.
     * @exception SQLException On input error.
     * @see SQLException
     */
        @Override
        public CustomerInfo mapRow(ResultSet resultSet, int i) throws SQLException {
            CustomerInfo customer = new CustomerInfo();
            try {
                customer.setId(resultSet.getLong("id"));
                customer.setFirstName(resultSet.getString("first_name"));
                customer.setLastName(resultSet.getString("last_name"));
                customer.setEmail(resultSet.getString("email"));
                customer.setPhoneNumber(resultSet.getString("phone_number"));
                customer.setCity(resultSet.getString("city"));
                customer.setCountry(resultSet.getString("country"));
                customer.setZipCode(resultSet.getString("zip_code"));
                customer.setIpAddress(resultSet.getString("ip_address"));
            }catch (Exception e){
                System.out.println(customer.getId());
                System.out.println(" problem is " + e.getCause());
            }

            return customer;
        }
}
