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
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String ticketId;

    private String ticketTitle;

    private String userId;

    private String userName;

    private double amount;

    private String status; // PENDING, PAID, CANCELLED

    private String invoiceNumber;

    private String paymentMethod; // CASH, CARD, BANK_TRANSFER

    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;
}
