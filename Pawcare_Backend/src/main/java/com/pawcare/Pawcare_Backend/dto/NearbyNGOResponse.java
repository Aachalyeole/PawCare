// dto/NearbyNGOResponse.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearbyNGOResponse {
    private Long ngoId;
    private String ngoName;
    private String name;
    private String email;
    private String phone;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String state;
    private Double distance; // in kilometers
    private Boolean isVerified;
    private String emergencyContact;
}