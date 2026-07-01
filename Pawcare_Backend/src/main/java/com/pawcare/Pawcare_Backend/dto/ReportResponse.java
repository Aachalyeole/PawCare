// dto/ReportResponse.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long reportId;
    private String animalType;
    private String condition;
    private String description;
    private Double latitude;
    private Double longitude;
    private String locationAddress;
    private String status;
    private List<String> imageUrls;
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private String assignedNGOName;
    private Long assignedNGOId;
    private Boolean isEmergency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String contactPhone;
    private String contactName;
}