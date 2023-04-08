package com.example.processapp.config;
/**
 * The class allows to process each item of customer data
 *
 * @version 1.0
 * @author Bappi Mazumder
 * @since 2023-03-31
 */
import com.example.processapp.entity.CustomerInfo;
import org.springframework.batch.item.ItemProcessor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerItemProcessor implements ItemProcessor<CustomerInfo,CustomerInfo> {
    private Set<CustomerInfo> seenCustomers = new HashSet<>();

    @Override
    public synchronized CustomerInfo process(CustomerInfo customerInfo) throws Exception {
        if(seenCustomers.contains(customerInfo)) {
             return null;
        }else if(emailValidation(customerInfo.getEmail()) == false
                || phoneNoValidation(customerInfo.getPhoneNumber())== false){
            customerInfo.setValid(false);
        }else{
            customerInfo.setValid(true);
        }
         seenCustomers.add(customerInfo);
        return customerInfo;
    }


    /**
     * This method helps to validate the email address
     *
     * @param emailAddress This is parameter to emailValidation method
     * @return This return true if email is valid, otherwise false.
     *
     */
    public boolean emailValidation(String emailAddress) {

        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(emailAddress);
        return  matcher.matches();
    }

    /**
     * This method helps to validate the phone number
     *
     * @param phoneNo This is parameter to phoneNo method
     * @return This return true if phone number is valid, otherwise false.
     *
     */
    public boolean phoneNoValidation(String phoneNo) {

        String regexPattern = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(phoneNo);
        return  matcher.matches();
    }
}
