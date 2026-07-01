// dto/UpdateStatusRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private String status;
    private String notes;
}