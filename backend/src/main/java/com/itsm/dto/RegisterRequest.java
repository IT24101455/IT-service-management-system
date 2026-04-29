package com.itsm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
            message = "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, one lowercase letter, one special character (@#$%^&+=!), and no whitespace")
    private String password;

    private String role = "USER"; // USER, TECHNICIAN, ADMIN

    private String phone;

    private String department;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "District is required")
    private String district;

    private String specialization;

    private String nicFrontUrl;
    private String nicBackUrl;

    private String otp;
}
