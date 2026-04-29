package com.itsm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leaves")
public class TechnicianLeave {

    @Id
    private String id;

    private String technicianId;

    private String technicianName;

    // e.g. SICK, VACATION, PERSONAL
    private String type;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;
    
    // URL to the uploaded medical report image (required for SICK leave)
    private String medicalReport;

    private String status = "PENDING"; // Requires Admin Approval

    private LocalDateTime createdAt = LocalDateTime.now();
}
