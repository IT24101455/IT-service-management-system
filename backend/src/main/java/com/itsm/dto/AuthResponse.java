package com.itsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String name;
    private String email;
    private String role;
    private String technicianReference;
    private boolean active;
    private String profilePicture;
}
