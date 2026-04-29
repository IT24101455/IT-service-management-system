package com.itsm.controller;

import com.itsm.dto.AuthResponse;
import com.itsm.dto.LoginRequest;
import com.itsm.dto.RegisterRequest;
import com.itsm.model.User;
import com.itsm.repository.UserRepository;
import com.itsm.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.itsm.service.OtpService;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        otpService.generateAndSendOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent successfully to " + request.getEmail());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        
        if (request.getOtp() == null || !otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        if (request.getPhone() != null && !request.getPhone().matches("^(?:0|\\+94|94)7[0-9]{8}$")) {
            return ResponseEntity.badRequest().body("Invalid Sri Lankan phone number. Use 07XXXXXXXX or +947XXXXXXXX format.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        if ("tharaniyajeyapalan29@gmail.com".equalsIgnoreCase(request.getEmail())) {
            user.setRole("ADMIN");
        } else {
            user.setRole(request.getRole());
        }

        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setProvince(request.getProvince());
        user.setDistrict(request.getDistrict());
        user.setSpecialization(request.getSpecialization());
        
        if ("TECHNICIAN".equals(user.getRole())) {
            if (request.getNicFrontUrl() == null || request.getNicBackUrl() == null) {
                return ResponseEntity.badRequest().body("NIC front and back photos are required for technicians");
            }
            String ref = "TECH-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            user.setTechnicianReference(ref);
            user.setNicFrontUrl(request.getNicFrontUrl());
            user.setNicBackUrl(request.getNicBackUrl());
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Special handling for the main admin account
            if ("tharaniyajeyapalan29@gmail.com".equalsIgnoreCase(request.getEmail()) && "Tharani@2001".equals(request.getPassword())) {
                User adminUser = userRepository.findByEmail(request.getEmail().toLowerCase()).orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName("Admin Tharani");
                    newUser.setEmail(request.getEmail().toLowerCase());
                    newUser.setPassword(passwordEncoder.encode("Tharani@2001"));
                    newUser.setRole("ADMIN");
                    newUser.setActive(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
                
                if (!"ADMIN".equals(adminUser.getRole())) {
                    adminUser.setRole("ADMIN");
                    userRepository.save(adminUser);
                }
                
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtUtils.generateToken(userDetails, adminUser.getId(), adminUser.getRole());
                return ResponseEntity.ok(new AuthResponse(token, adminUser.getId(), adminUser.getName(), adminUser.getEmail(), adminUser.getRole(), adminUser.getTechnicianReference(), adminUser.isActive(), adminUser.getProfilePicture()));
            }

            // Standard login for other users
            User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
            
            if (!user.getRole().equals(request.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Please login using the correct option.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails, user.getId(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getTechnicianReference(), user.isActive(), user.getProfilePicture()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email not found");
        }
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP sent to " + email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (otp == null || !otpService.verifyOtp(email, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }
}
