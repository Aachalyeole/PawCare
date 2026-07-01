package com.pawcare.Pawcare_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String location;
}
