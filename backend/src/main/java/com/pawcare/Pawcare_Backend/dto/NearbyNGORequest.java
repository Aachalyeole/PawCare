// dto/NearbyNGORequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class NearbyNGORequest {
    private Double latitude;
    private Double longitude;
    private Double radius; // in kilometers (default 10km if not provided)
}