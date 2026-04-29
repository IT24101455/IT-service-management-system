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
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;

    private String title;

    private String message;

    private String type; // TICKET_CREATED, TICKET_UPDATED, TICKET_RESOLVED, PAYMENT, SYSTEM

    private boolean isRead = false;

    private String ticketId;

    private LocalDateTime createdAt = LocalDateTime.now();
}
