package com.itsm.repository;

import com.itsm.model.TechnicianSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TechnicianSubscriptionRepository extends MongoRepository<TechnicianSubscription, String> {
    List<TechnicianSubscription> findByTechnicianId(String technicianId);
    List<TechnicianSubscription> findByStatus(String status);
    List<TechnicianSubscription> findByMonthAndYear(String month, int year);
    List<TechnicianSubscription> findByMonthAndYearAndStatus(String month, int year, String status);
}
