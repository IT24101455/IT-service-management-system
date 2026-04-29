package com.itsm.service;

import com.itsm.model.Notification;
import com.itsm.model.Ticket;
import com.itsm.model.User;
import com.itsm.repository.NotificationRepository;
import com.itsm.repository.TicketRepository;
import com.itsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorService {

    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Run every minute to check for SLA breaches
    @Scheduled(cron = "0 * * * * *")
    public void checkSlaBreaches() {
        log.info("Checking for SLA breached tickets...");
        List<String> closedStatuses = Arrays.asList("RESOLVED", "CLOSED");
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> breachedTickets = ticketRepository.findByStatusNotInAndSlaDeadlineBeforeAndSlaBreachedFalse(
                closedStatuses, now);

        for (Ticket ticket : breachedTickets) {
            log.warn("SLA Breached for Ticket: {} | Current Technician: {}", ticket.getId(), ticket.getTechnicianId());

            // Mark as breached immediately to prevent reprocessing
            ticket.setSlaBreached(true);
            ticket.setUpdatedAt(LocalDateTime.now());

            String oldTechnicianId = ticket.getTechnicianId();
            String oldTechnicianName = ticket.getTechnicianName();

            // --- Step 1: Notify the current (breaching) technician ---
            if (oldTechnicianId != null && !oldTechnicianId.isEmpty()) {
                sendNotification(
                    oldTechnicianId,
                    "⚠ SLA Deadline Missed – Ticket Reassigned",
                    "Ticket '" + ticket.getTitle() + "' was not resolved within the SLA deadline and has been reassigned to another technician.",
                    "SLA_BREACH",
                    ticket.getId()
                );
            }

            // --- Step 2: Find the best replacement technician ---
            Optional<User> replacement = findBestReplacement(ticket, oldTechnicianId);

            if (replacement.isPresent()) {
                User newTech = replacement.get();
                log.info("Reassigning Ticket {} to Technician: {} ({})", ticket.getId(), newTech.getName(), newTech.getId());

                // Reassign the ticket
                ticket.setTechnicianId(newTech.getId());
                ticket.setTechnicianName(newTech.getName());
                ticket.setStatus("IN_PROGRESS");

                // --- Step 3: Notify the new technician ---
                sendNotification(
                    newTech.getId(),
                    "🎯 Ticket Reassigned to You (SLA Recovery)",
                    "Ticket '" + ticket.getTitle() + "' has been reassigned to you due to an SLA breach by the previous technician. Please resolve it urgently.",
                    "TICKET_ASSIGNED",
                    ticket.getId()
                );

                // --- Step 4: Notify the user ---
                sendNotification(
                    ticket.getUserId(),
                    "🔄 Your Ticket Has Been Reassigned",
                    "Your ticket '" + ticket.getTitle() + "' was reassigned to " + newTech.getName() + " to ensure timely resolution.",
                    "TICKET_UPDATED",
                    ticket.getId()
                );

                log.info("Ticket {} successfully reassigned from {} to {}", ticket.getId(), oldTechnicianName, newTech.getName());
            } else {
                // No replacement found – escalate to unassigned queue and notify user
                log.warn("No available replacement technician found for Ticket {}. Escalating to unassigned.", ticket.getId());
                ticket.setTechnicianId(null);
                ticket.setTechnicianName(null);
                ticket.setStatus("PENDING");

                // Notify user
                sendNotification(
                    ticket.getUserId(),
                    "⚠ Ticket Escalated – Pending Reassignment",
                    "Your ticket '" + ticket.getTitle() + "' missed its SLA deadline. Our team is urgently finding a new technician for you.",
                    "TICKET_UPDATED",
                    ticket.getId()
                );
            }

            ticketRepository.save(ticket);
        }

        if (!breachedTickets.isEmpty()) {
            log.info("Processed {} SLA breached tickets.", breachedTickets.size());
        }
    }

    /**
     * Finds the best available replacement technician:
     * 1. Must be active
     * 2. Must not be the same technician who breached
     * 3. Prefers matching specialization with the ticket's category
     * 4. Picks the one with the fewest active (non-resolved) tickets (lightest workload)
     */
    private Optional<User> findBestReplacement(Ticket ticket, String excludeTechnicianId) {
        List<User> activeTechnicians = userRepository.findByRoleAndActive("TECHNICIAN", true);

        return activeTechnicians.stream()
            // Exclude the current breaching technician
            .filter(t -> !t.getId().equals(excludeTechnicianId))
            // Prefer technicians whose specialization matches the ticket category
            .sorted(Comparator
                // 1. Matching specialization gets priority (false = 0 sorts first)
                .<User, Boolean>comparing(t -> !matchesCategory(t.getSpecialization(), ticket.getCategory()))
                // 2. Among equally-ranked, pick the one with fewest active tickets
                .thenComparingLong(t -> ticketRepository.countByTechnicianId(t.getId())
                    - ticketRepository.countByTechnicianIdAndStatus(t.getId(), "RESOLVED")))
            .findFirst();
    }

    /**
     * Checks if a technician's specialization matches the ticket's category.
     * e.g. specialization "SOFTWARE" matches category "SOFTWARE"
     */
    private boolean matchesCategory(String specialization, String category) {
        if (specialization == null || category == null) return false;
        return specialization.equalsIgnoreCase(category);
    }

    /**
     * Saves and sends a real-time WebSocket notification.
     */
    private void sendNotification(String userId, String title, String message, String type, String ticketId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setTicketId(ticketId);

        Notification saved = notificationRepository.save(n);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", saved);
    }
}
