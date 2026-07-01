// dto/NGODetailsDTO.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NGODetailsDTO {
    private Long ngoId;
    private String ngoName;
    private String registrationNumber;
    private String description;
    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String emergencyContact;
    private Boolean isVerified;
}