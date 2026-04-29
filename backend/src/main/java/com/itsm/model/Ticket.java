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
@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    private String title;

    private String description;

    private String category; // SOFTWARE, HARDWARE, NETWORK, OTHER

    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    private String status; // PENDING, IN_PROGRESS, RESOLVED, CLOSED

    private String userId;

    private String userName;

    private String technicianId;

    private String technicianName;

    private String province;

    private String district;

    private String attachmentUrl;

    private String solution;

    private LocalDateTime slaDeadline;

    private Integer rating;

    private String ratingComment;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    private boolean slaBreached;
}
