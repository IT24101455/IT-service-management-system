package com.itsm.service;

import com.itsm.model.TechnicianSubscription;
import com.itsm.model.User;
import com.itsm.repository.TechnicianSubscriptionRepository;
import com.itsm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TechnicianSubscriptionService {

    @Autowired
    private TechnicianSubscriptionRepository repository;

    @Autowired
    private UserRepository userRepository;

    public TechnicianSubscription submitSubscription(TechnicianSubscription subscription) {
        subscription.setStatus("PENDING");
        subscription.setSubmissionDate(LocalDateTime.now());
        return repository.save(subscription);
    }

    public List<TechnicianSubscription> getTechnicianHistory(String technicianId) {
        return repository.findByTechnicianId(technicianId);
    }

    public List<TechnicianSubscription> getAllSubscriptions() {
        return repository.findAll();
    }

    public List<TechnicianSubscription> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    public TechnicianSubscription approveSubscription(String id, String adminId) {
        Optional<TechnicianSubscription> opt = repository.findById(id);
        if (opt.isPresent()) {
            TechnicianSubscription sub = opt.get();
            sub.setStatus("APPROVED");
            sub.setVerifiedAt(LocalDateTime.now());
            sub.setVerifiedBy(adminId);
            
            // Reactivate technician account
            Optional<User> userOpt = userRepository.findById(sub.getTechnicianId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("TechnicianSubscriptionService: Activating user: " + user.getEmail() + " (ID: " + user.getId() + ")");
                user.setActive(true);
                userRepository.save(user);
                System.out.println("TechnicianSubscriptionService: User activated successfully.");
            } else {
                System.err.println("TechnicianSubscriptionService: Could not find user with ID: " + sub.getTechnicianId() + " to activate!");
            }
            
            return repository.save(sub);
        }
        return null;
    }

    public TechnicianSubscription rejectSubscription(String id, String adminId, String reason) {
        Optional<TechnicianSubscription> opt = repository.findById(id);
        if (opt.isPresent()) {
            TechnicianSubscription sub = opt.get();
            sub.setStatus("REJECTED");
            sub.setRejectionReason(reason);
            sub.setVerifiedAt(LocalDateTime.now());
            sub.setVerifiedBy(adminId);
            return repository.save(sub);
        }
        return null;
    }

    public void deactivateUnpaidTechnicians() {
        LocalDateTime now = LocalDateTime.now();
        String currentMonth = now.getMonth().name().substring(0, 1) + now.getMonth().name().substring(1).toLowerCase();
        int currentYear = now.getYear();

        List<User> technicians = userRepository.findByRole("TECHNICIAN");
        for (User tech : technicians) {
            List<TechnicianSubscription> subs = repository.findByMonthAndYearAndStatus(currentMonth, currentYear, "APPROVED");
            boolean hasPaid = subs.stream().anyMatch(s -> s.getTechnicianId().equals(tech.getId()));
            
            if (!hasPaid && tech.isActive()) {
                tech.setActive(false);
                userRepository.save(tech);
                // In a real system, you might send a notification here
            }
        }
    }
}
