package com.itsm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otps")
public class Otp {

    @Id
    private String id;

    @Indexed
    private String email;

    private String otpCode;

    private LocalDateTime expiryTime;

    public Otp(String email, String otpCode, int expiryMinutes) {
        this.email = email;
        this.otpCode = otpCode;
        this.expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
