package com.itsm.controller;

import com.itsm.model.User;
import com.itsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final com.itsm.service.CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // hide password
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id).map(u -> {
            u.setPassword(null);
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role, @RequestParam(required = false) Boolean activeOnly) {
        List<User> users;
        if (Boolean.TRUE.equals(activeOnly)) {
            users = userRepository.findByRoleAndActive(role.toUpperCase(), true);
        } else {
            users = userRepository.findByRole(role.toUpperCase());
        }
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/technicians/available")
    public ResponseEntity<List<User>> getAvailableTechnicians() {
        List<User> technicians = userRepository.findByRoleAndActive("TECHNICIAN", true);
        technicians.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(technicians);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User updated) {
        return userRepository.findById(id).map(user -> {
            if (updated.getName() != null) user.setName(updated.getName());
            if (updated.getPhone() != null) {
                if (!updated.getPhone().matches("^(?:0|\\+94|94)7[0-9]{8}$")) {
                    // Return custom error response for invalid phone
                    return ResponseEntity.badRequest().body(null); 
                }
                user.setPhone(updated.getPhone());
            }
            if (updated.getDepartment() != null) user.setDepartment(updated.getDepartment());
            if (updated.getProvince() != null) user.setProvince(updated.getProvince());
            if (updated.getDistrict() != null) user.setDistrict(updated.getDistrict());
            if (updated.getRole() != null) user.setRole(updated.getRole());
            if (updated.getSpecialization() != null) user.setSpecialization(updated.getSpecialization());
            if (updated.getExperienceYears() != null) user.setExperienceYears(updated.getExperienceYears());
            if (updated.getWorkingDays() != null) user.setWorkingDays(updated.getWorkingDays());
            if (updated.getWorkingStartTime() != null) user.setWorkingStartTime(updated.getWorkingStartTime());
            if (updated.getWorkingEndTime() != null) user.setWorkingEndTime(updated.getWorkingEndTime());
            user.setActive(updated.isActive());
            user.setUpdatedAt(LocalDateTime.now());
            User saved = userRepository.save(user);
            saved.setPassword(null);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable String id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(!user.isActive());
            user.setUpdatedAt(LocalDateTime.now());
            User saved = userRepository.save(user);
            saved.setPassword(null);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        System.out.println("UserController: Received profile picture upload request for user ID: " + id);
        return userRepository.findById(id).map(user -> {
            try {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body("File is empty");
                }
                
                String fileUrl = cloudinaryService.uploadFile(file, "profiles");
                user.setProfilePicture(fileUrl);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                return ResponseEntity.ok(java.util.Map.of("url", fileUrl));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Could not upload file to Cloudinary: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<?> removeProfilePicture(@PathVariable String id) {
        System.out.println("Removing profile picture for user: " + id);
        return userRepository.findById(id).map(user -> {
            try {
                String currentPic = user.getProfilePicture();
                if (currentPic != null && !currentPic.isEmpty()) {
                    // Normalize the path to delete the physical file
                    String fileName = currentPic.replace("/uploads/profiles/", "");
                    Path filePath = Paths.get("uploads/profiles").resolve(fileName).toAbsolutePath().normalize();
                    Files.deleteIfExists(filePath);
                    System.out.println("Deleted file: " + filePath);
                }
                
                user.setProfilePicture(null);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                System.out.println("Profile picture record cleared for user: " + id);
                return ResponseEntity.ok("Profile picture removed");
            } catch (Exception e) {
                System.err.println("Removal failed for user " + id + ": " + e.getMessage());
                return ResponseEntity.internalServerError().body("Could not remove file: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

}

