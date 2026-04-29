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
@Document(collection = "assets")
public class Asset {

    @Id
    private String id;

    private String name;

    private String type; // LAPTOP, DESKTOP, PRINTER, SERVER, NETWORK, OTHER

    private String serialNumber;

    private String model;

    private String manufacturer;

    private String status; // ACTIVE, INACTIVE, UNDER_MAINTENANCE, RETIRED

    private String assignedToUserId;

    private String assignedToUserName;

    private String location;

    private String purchaseDate;

    private String warrantyExpiry;

    private double purchaseCost;

    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
