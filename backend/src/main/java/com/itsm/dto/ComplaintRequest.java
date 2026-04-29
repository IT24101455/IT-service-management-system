package com.itsm.dto;

import lombok.Data;

@Data
public class ComplaintRequest {
    private String technicianId;
    private String description;
}
