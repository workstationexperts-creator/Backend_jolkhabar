package com.jolkhabar.JolkhabarBackend.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable // Embedded inside Order (fields stored inline)
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private String recipientName;  // Full name of recipient
    private String phoneNumber;    // Contact number
    private String street;         // Street / Flat / Locality
    private String city;
    private String state;
    private String postalCode;
    
    @Builder.Default
    private String country = "India";  // Default country fallback
}
