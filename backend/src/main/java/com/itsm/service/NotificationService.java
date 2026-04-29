package com.itsm.service;

import com.itsm.model.Notification;
import com.itsm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification to a specific user via database and WebSocket.
     */
    public void sendNotification(String userId, String title, String message, String type, String ticketId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTicketId(ticketId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        // Save to Database
        Notification saved = notificationRepository.save(notification);

        // Push via WebSocket
        // Destination: /user/{userId}/queue/notifications
        try {
            messagingTemplate.convertAndSendToUser(
                userId, 
                "/queue/notifications", 
                saved
            );
            System.out.println("WebSocket notification sent to user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
}
