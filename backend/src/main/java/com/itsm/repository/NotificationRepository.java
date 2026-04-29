package com.itsm.repository;

import com.itsm.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserIdAndIsRead(String userId, boolean isRead);
    long countByUserIdAndIsRead(String userId, boolean isRead);
}
