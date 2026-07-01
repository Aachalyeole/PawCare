// dto/NGOSignupRequest.java
package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class NGOSignupRequest {
    // User fields
    private String name;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String location;

    // NGO specific fields
    private String ngoName;
    private String registrationNumber;
    private String description;
    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String pincode;
    private String website;
    private String emergencyContact;
}