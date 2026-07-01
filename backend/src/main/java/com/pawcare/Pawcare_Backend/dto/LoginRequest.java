// dto/LoginRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}