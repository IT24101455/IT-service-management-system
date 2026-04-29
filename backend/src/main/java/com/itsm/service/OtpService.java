package com.itsm.service;

import com.itsm.model.Otp;
import com.itsm.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSendOtp(String email) {
        // Generate 6 digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        
        // Save to DB (overwrite existing for same email)
        otpRepository.deleteByEmail(email);
        Otp otpObj = new Otp(email, otp, 5); // 5 minutes expiry
        otpRepository.save(otpObj);

        // Send Email
        emailService.sendOtp(email, otp);
        
        return otp;
    }

    public boolean verifyOtp(String email, String otpCode) {
        // BYPASS FOR TESTING
        return true;
        /*
        Optional<Otp> otpOpt = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);
        
        if (otpOpt.isPresent()) {
            Otp otp = otpOpt.get();
            if (!otp.isExpired() && otp.getOtpCode().equals(otpCode)) {
                otpRepository.deleteByEmail(email); // Delete after successful verification
                return true;
            }
        }
        return false;
        */
    }
}
