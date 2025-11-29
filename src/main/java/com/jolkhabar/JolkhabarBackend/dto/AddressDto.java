package com.jolkhabar.JolkhabarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    private String recipientName;
    private String phoneNumber;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String razorpayOrderId;

}