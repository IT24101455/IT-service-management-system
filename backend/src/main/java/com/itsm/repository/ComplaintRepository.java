package com.itsm.repository;

import com.itsm.model.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    List<Complaint> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Complaint> findByTechnicianIdOrderByCreatedAtDesc(String technicianId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
