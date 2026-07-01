// dto/LoginResponse.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Boolean isEmailVerified;
    private Boolean isNGOVerified;
    private NGODetailsDTO ngoDetails;

    // Constructor for regular users (without NGO details)
    public LoginResponse(Long userId, String name, String email, String phone, String role,
                         Boolean isEmailVerified, Boolean isNGOVerified) {
        this(userId, name, email, phone, role, isEmailVerified, isNGOVerified, null);
    }
}