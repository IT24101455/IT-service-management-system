package com.itsm.repository;

import com.itsm.model.TechnicianLeave;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRepository extends MongoRepository<TechnicianLeave, String> {
    List<TechnicianLeave> findByTechnicianIdOrderByStartDateDesc(String technicianId);
}
