package com.itsm.controller;

import com.itsm.model.TechnicianLeave;
import com.itsm.model.User;
import com.itsm.model.Notification;
import com.itsm.repository.LeaveRepository;
import com.itsm.repository.UserRepository;
import com.itsm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.itsm.service.CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<TechnicianLeave>> getAllLeaves() {
        // Find all leaves (Admins)
        return ResponseEntity.ok(leaveRepository.findAll());
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TechnicianLeave>> getLeavesByTechnician(@PathVariable String technicianId) {
        return ResponseEntity.ok(leaveRepository.findByTechnicianIdOrderByStartDateDesc(technicianId));
    }

    @PostMapping
    public ResponseEntity<TechnicianLeave> createLeave(@RequestBody TechnicianLeave leave) {
        leave.setCreatedAt(LocalDateTime.now());
        if (leave.getStatus() == null) {
            leave.setStatus("PENDING");
        }
        TechnicianLeave savedLeave = leaveRepository.save(leave);

        // Notify all admins about the new leave request
        List<User> admins = userRepository.findByRole("ADMIN");
        for (User admin : admins) {
            Notification n = new Notification();
            n.setUserId(admin.getId());
            n.setTitle("New Leave Request");
            n.setMessage(leave.getTechnicianName() + " requested time off starting " + leave.getStartDate());
            n.setType("SYSTEM");
            Notification savedNotif = notificationRepository.save(n);
            messagingTemplate.convertAndSendToUser(admin.getId(), "/queue/notifications", savedNotif);
        }

        return ResponseEntity.ok(savedLeave);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TechnicianLeave> updateLeaveStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return leaveRepository.findById(id).map(leave -> {
            leave.setStatus(status);
            TechnicianLeave updatedLeave = leaveRepository.save(leave);

            // Notify the technician that their leave status was updated
            Notification n = new Notification();
            n.setUserId(leave.getTechnicianId());
            n.setTitle("Leave Request " + status);
            n.setMessage("Your leave request for " + leave.getStartDate() + " has been " + status.toLowerCase() + ".");
            n.setType("SYSTEM");
            Notification savedNotif = notificationRepository.save(n);
            messagingTemplate.convertAndSendToUser(leave.getTechnicianId(), "/queue/notifications", savedNotif);

            return ResponseEntity.ok(updatedLeave);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-report")
    public ResponseEntity<?> uploadMedicalReport(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String fileUrl = cloudinaryService.uploadFile(file, "medical_reports");
            return ResponseEntity.ok(java.util.Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Could not upload file to Cloudinary: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeave(@PathVariable String id) {
        leaveRepository.deleteById(id);
        return ResponseEntity.ok("Leave request cancelled successfully");
    }
}
