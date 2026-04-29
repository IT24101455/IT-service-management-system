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
@Document(collection = "complaints")
public class Complaint {
    @Id
    private String id;
    
    private String userId;
    private String userName;
    
    private String technicianId;
    private String technicianName;
    
    private String description;
    
    private String status = "PENDING"; // PENDING, RESOLVED
    private String resolutionNotes;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
