package com.itsm.repository;

import com.itsm.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByUserId(String userId);
    List<Ticket> findByTechnicianId(String technicianId);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByCategory(String category);
    List<Ticket> findByPriority(String priority);
    long countByStatus(String status);
    long countByTechnicianId(String technicianId);
    long countByTechnicianIdAndStatus(String technicianId, String status);
    List<Ticket> findByTechnicianIdOrderByCreatedAtDesc(String technicianId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    List<Ticket> findByStatusNotInAndSlaDeadlineBeforeAndSlaBreachedFalse(List<String> statuses, java.time.LocalDateTime now);
}
