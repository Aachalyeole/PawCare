// dto/ReportRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String animalType;
    private String condition;
    private String description;
    private Double latitude;
    private Double longitude;
    private String locationAddress;
    private Boolean isEmergency;
    private String contactPhone;
    private String contactName;
    private String userEmail;
}