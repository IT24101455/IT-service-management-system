package com.itsm.controller;

import com.itsm.model.ChatMessage;
import com.itsm.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        ChatMessage saved = chatMessageRepository.save(message);
        
        // Notify the receiver in real-time
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId(), 
            "/queue/messages", 
            saved
        );
        
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<ChatMessage>> getConversation(
            @RequestParam String user1, 
            @RequestParam String user2) {
        return ResponseEntity.ok(chatMessageRepository.findConversation(user1, user2));
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<ChatMessage>> getInbox(@PathVariable String userId) {
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByTimestampDesc(userId));
    }
}
