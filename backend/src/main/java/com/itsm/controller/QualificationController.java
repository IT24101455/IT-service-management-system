package com.itsm.controller;

import com.itsm.model.User;
import com.itsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final UserRepository userRepository;

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addQualification(@PathVariable String userId, @RequestBody User.Qualification qual) {
        System.out.println("--- QualificationController: Adding for user " + userId + " ---");
        return userRepository.findById(userId).map(user -> {
            if (user.getQualifications() == null) {
                user.setQualifications(new ArrayList<>());
            }
            user.getQualifications().add(qual);
            user.setUpdatedAt(LocalDateTime.now());
            User saved = userRepository.save(user);
            saved.setPassword(null);
            System.out.println("Success: Total qualifications = " + saved.getQualifications().size());
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> {
            System.out.println("Error: User " + userId + " not found");
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/user/{userId}/{qualId}")
    public ResponseEntity<?> removeQualification(@PathVariable String userId, @PathVariable String qualId) {
        System.out.println("--- QualificationController: Removing " + qualId + " from user " + userId + " ---");
        return userRepository.findById(userId).map(user -> {
            if (user.getQualifications() != null) {
                user.getQualifications().removeIf(q -> q.getId().equals(qualId));
                user.setUpdatedAt(LocalDateTime.now());
                User saved = userRepository.save(user);
                saved.setPassword(null);
                return ResponseEntity.ok(saved);
            }
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}
