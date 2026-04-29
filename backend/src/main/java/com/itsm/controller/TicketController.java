package com.itsm.controller;

import com.itsm.model.Ticket;
import com.itsm.model.Notification;
import com.itsm.repository.TicketRepository;
import com.itsm.repository.NotificationRepository;
import com.itsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ticketRepository.findByUserId(userId));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<Ticket>> getTicketsByTechnician(@PathVariable String technicianId) {
        return ResponseEntity.ok(ticketRepository.findByTechnicianIdOrderByCreatedAtDesc(technicianId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ticketRepository.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        ticket.setStatus("PENDING");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setSlaDeadline(calculateSlaDeadline(ticket.getPriority()));

        // If technician is assigned during creation
        if (ticket.getTechnicianId() != null && !ticket.getTechnicianId().isEmpty()) {
            ticket.setStatus("OPEN"); // Set to OPEN if assigned
        }

        Ticket saved = ticketRepository.save(ticket);

        // Create notification for admin
        Notification adminNotif = new Notification();
        adminNotif.setTitle("New Ticket Created");
        adminNotif.setMessage("Ticket '" + ticket.getTitle() + "' has been submitted by " + ticket.getUserName());
        adminNotif.setType("TICKET_CREATED");
        adminNotif.setTicketId(saved.getId());
        adminNotif.setUserId(ticket.getUserId()); // In a real app, this might go to all admins
        notificationRepository.save(adminNotif);

        // Notify assigned technician if present
        if (ticket.getTechnicianId() != null && !ticket.getTechnicianId().isEmpty()) {
            Notification techNotif = new Notification();
            techNotif.setUserId(ticket.getTechnicianId());
            techNotif.setTitle("New Ticket Assigned");
            techNotif.setMessage("You have been assigned a new ticket: " + ticket.getTitle());
            techNotif.setType("TICKET_ASSIGNED");
            techNotif.setTicketId(saved.getId());
            Notification savedTechNotif = notificationRepository.save(techNotif);
            messagingTemplate.convertAndSendToUser(ticket.getTechnicianId(), "/queue/notifications", savedTechNotif);
        }

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id, @RequestBody Ticket updated) {
        return ticketRepository.findById(id).map(ticket -> {
            if (updated.getTitle() != null) ticket.setTitle(updated.getTitle());
            if (updated.getDescription() != null) ticket.setDescription(updated.getDescription());
            if (updated.getCategory() != null) ticket.setCategory(updated.getCategory());
            if (updated.getPriority() != null) {
                // Recalculate SLA if priority changes
                if (!updated.getPriority().equals(ticket.getPriority())) {
                    ticket.setSlaDeadline(calculateSlaDeadline(updated.getPriority()));
                }
                ticket.setPriority(updated.getPriority());
            }
            if (updated.getStatus() != null) {
                ticket.setStatus(updated.getStatus());
                
                // Notify user of status change
                Notification n = new Notification();
                n.setUserId(ticket.getUserId());
                n.setTitle("Ticket Status Updated");
                n.setMessage("Your ticket '" + ticket.getTitle() + "' is now " + updated.getStatus() + ".");
                n.setType("TICKET_UPDATED");
                n.setTicketId(id);
                
                if ("RESOLVED".equals(updated.getStatus())) {
                    ticket.setResolvedAt(LocalDateTime.now());
                    n.setTitle("Ticket Resolved");
                    n.setMessage("Your ticket '" + ticket.getTitle() + "' has been resolved.");
                    n.setType("TICKET_RESOLVED");
                }
                
                Notification savedNotif = notificationRepository.save(n);
                messagingTemplate.convertAndSendToUser(ticket.getUserId(), "/queue/notifications", savedNotif);
            }
            if (updated.getTechnicianId() != null) ticket.setTechnicianId(updated.getTechnicianId());
            if (updated.getTechnicianName() != null) ticket.setTechnicianName(updated.getTechnicianName());
            if (updated.getSolution() != null) ticket.setSolution(updated.getSolution());
            if (updated.getRating() != null) ticket.setRating(updated.getRating());
            if (updated.getRatingComment() != null) ticket.setRatingComment(updated.getRatingComment());
            ticket.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<?> assignTechnician(@PathVariable String id, @RequestBody Map<String, String> body) {
        String techId = body.get("technicianId");
        
        // Validate technician status
        if (techId != null) {
            com.itsm.model.User tech = userRepository.findById(techId).orElse(null);
            if (tech != null && !tech.isActive()) {
                return ResponseEntity.badRequest().body("Cannot assign to an inactive technician");
            }
        }

        return ticketRepository.findById(id).map(ticket -> {
            ticket.setTechnicianId(techId);
            ticket.setTechnicianName(body.get("technicianName"));
            ticket.setStatus("IN_PROGRESS");
            ticket.setUpdatedAt(LocalDateTime.now());

            Notification n = new Notification();
            n.setUserId(ticket.getUserId());
            n.setTitle("Ticket Assigned");
            n.setMessage("Your ticket has been assigned to " + body.get("technicianName"));
            n.setType("TICKET_UPDATED");
            n.setTicketId(id);
            Notification savedNotif = notificationRepository.save(n);
            messagingTemplate.convertAndSendToUser(ticket.getUserId(), "/queue/notifications", savedNotif);

            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable String id) {
        ticketRepository.deleteById(id);
        return ResponseEntity.ok("Ticket deleted");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
            "total", ticketRepository.count(),
            "pending", ticketRepository.countByStatus("PENDING"),
            "inProgress", ticketRepository.countByStatus("IN_PROGRESS"),
            "resolved", ticketRepository.countByStatus("RESOLVED"),
            "closed", ticketRepository.countByStatus("CLOSED")
        ));
    }

    private LocalDateTime calculateSlaDeadline(String priority) {
        if (priority == null) return LocalDateTime.now().plusHours(24);
        switch (priority.toUpperCase()) {
            case "CRITICAL": return LocalDateTime.now().plusHours(2);
            case "HIGH": return LocalDateTime.now().plusHours(4);
            case "LOW": return LocalDateTime.now().plusHours(48);
            default: return LocalDateTime.now().plusHours(24);
        }
    }
}
