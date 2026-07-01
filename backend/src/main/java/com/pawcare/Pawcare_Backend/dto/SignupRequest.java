// dto/SignupRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String location;
}