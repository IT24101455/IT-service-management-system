package com.itsm.controller;

import com.itsm.dto.ComplaintRequest;
import com.itsm.model.Complaint;
import com.itsm.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@RequestBody ComplaintRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(complaintService.createComplaint(email, request));
    }

    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Complaint>> getUserComplaints(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(complaintService.getUserComplaints(email));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Complaint> resolveComplaint(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String resolutionNotes = payload.get("resolutionNotes");
        return ResponseEntity.ok(complaintService.resolveComplaint(id, resolutionNotes));
    }
}
