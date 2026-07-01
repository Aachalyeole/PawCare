// dto/OtpVerificationRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otp;
}