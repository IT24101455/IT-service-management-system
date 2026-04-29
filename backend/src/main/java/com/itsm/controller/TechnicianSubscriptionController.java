package com.itsm.controller;

import com.itsm.model.TechnicianSubscription;
import com.itsm.service.TechnicianSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/technician-subscriptions")
@CrossOrigin(origins = "*")
public class TechnicianSubscriptionController {

    @Autowired
    private TechnicianSubscriptionService service;

    @Autowired
    private com.itsm.service.CloudinaryService cloudinaryService;

    @PostMapping("/upload-slip")
    public ResponseEntity<?> uploadSlip(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String fileUrl = cloudinaryService.uploadFile(file, "technician_payments");
            return ResponseEntity.ok(java.util.Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Could not upload file to Cloudinary: " + e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<TechnicianSubscription> submit(@RequestBody TechnicianSubscription subscription) {
        return ResponseEntity.ok(service.submitSubscription(subscription));
    }

    @GetMapping("/technician/{id}")
    public ResponseEntity<List<TechnicianSubscription>> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(service.getTechnicianHistory(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TechnicianSubscription>> getAll() {
        return ResponseEntity.ok(service.getAllSubscriptions());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TechnicianSubscription>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TechnicianSubscription> approve(@PathVariable String id, @RequestParam String adminId) {
        System.out.println("TechnicianSubscriptionController: Approving subscription ID: " + id + " by admin: " + adminId);
        TechnicianSubscription sub = service.approveSubscription(id, adminId);
        if (sub != null) return ResponseEntity.ok(sub);
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TechnicianSubscription> reject(
            @PathVariable String id, 
            @RequestParam String adminId, 
            @RequestParam String reason) {
        System.out.println("TechnicianSubscriptionController: Rejecting subscription ID: " + id + " by admin: " + adminId + ". Reason: " + reason);
        TechnicianSubscription sub = service.rejectSubscription(id, adminId, reason);
        if (sub != null) return ResponseEntity.ok(sub);
        return ResponseEntity.notFound().build();
    }
}
