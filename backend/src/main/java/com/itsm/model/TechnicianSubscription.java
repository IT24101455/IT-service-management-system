package com.itsm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "technician_subscriptions")
public class TechnicianSubscription {

    @Id
    private String id;

    private String technicianId;
    private String technicianName;
    private double amount = 1500.0;
    private String paymentDate; // Submission date or user entered date
    private String technicianReference; // The unique ID assigned to the technician
    private String referenceNumber; // Bank reference number entered by technician
    private String paymentSlipUrl;
    
    private String month; // e.g., "April"
    private int year; // e.g., 2026
    
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private String rejectionReason;

    private LocalDateTime submissionDate = LocalDateTime.now();
    private LocalDateTime verifiedAt;
    private String verifiedBy; // Admin ID
}
