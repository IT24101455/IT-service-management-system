package com.itsm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String role; // USER, TECHNICIAN, ADMIN

    private String phone;

    private String department;
    private String province;
    private String district;
    private String specialization; // SOFTWARE, HARDWARE, etc.
    
    private Integer experienceYears;
    
    // Technician Working Schedule
    private String workingDays;
    private String workingStartTime;
    private String workingEndTime;

    private boolean active = true;
    private String profilePicture;
    private String technicianReference; // Unique ID for subscription (e.g., TECH-2024-001)
    private String nicFrontUrl;
    private String nicBackUrl;

    private java.util.List<Qualification> qualifications = new java.util.ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Qualification {
        private String id = java.util.UUID.randomUUID().toString();
        private String title;
        private String institution;
        private String year;
        private String certificateUrl;
    }
}
