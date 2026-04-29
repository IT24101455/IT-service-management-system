package com.itsm.service;

import com.itsm.dto.ComplaintRequest;
import com.itsm.model.Complaint;
import com.itsm.model.User;
import com.itsm.repository.ComplaintRepository;
import com.itsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public Complaint createComplaint(String email, ComplaintRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        Complaint complaint = new Complaint();
        complaint.setUserId(user.getId());
        complaint.setUserName(user.getName());
        complaint.setTechnicianId(technician.getId());
        complaint.setTechnicianName(technician.getName());
        complaint.setDescription(request.getDescription());
        complaint.setStatus("PENDING");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);

        // Notify all Admin users
        List<User> admins = userRepository.findByRole("ADMIN");
        String adminTitle = "New Complaint Filed";
        String adminMsg = "User " + user.getName() + " has filed a complaint against " + technician.getName();
        
        for (User admin : admins) {
            notificationService.sendNotification(
                admin.getId(),
                adminTitle,
                adminMsg,
                "SYSTEM",
                null
            );
        }

        // Broadcast to refresh Admin Complaints page
        messagingTemplate.convertAndSend("/topic/complaints/refresh", "REFRESH");

        return saved;
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Complaint> getUserComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Complaint resolveComplaint(String complaintId, String resolutionNotes) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        complaint.setStatus("RESOLVED");
        complaint.setResolutionNotes(resolutionNotes);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        return complaintRepository.save(complaint);
    }
}
