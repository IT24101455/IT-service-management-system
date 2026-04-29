package com.itsm.controller;

import com.itsm.repository.TicketRepository;
import com.itsm.repository.UserRepository;
import com.itsm.repository.PaymentRepository;
import com.itsm.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Object> report = new HashMap<>();
        report.put("totalTickets", tickets.size());
        report.put("pending", tickets.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
        report.put("inProgress", tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count());
        report.put("resolved", tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count());
        report.put("closed", tickets.stream().filter(t -> "CLOSED".equals(t.getStatus())).count());
        report.put("totalUsers", userRepository.findByRole("USER").size());
        report.put("totalTechnicians", userRepository.findByRole("TECHNICIAN").size());
        report.put("totalPayments", paymentRepository.count());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<Map<String, Long>> getCategoryBreakdown() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Long> breakdown = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory() != null ? t.getCategory() : "OTHER", Collectors.counting()));
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/technician-performance")
    public ResponseEntity<List<Map<String, Object>>> getTechnicianPerformance() {
        List<com.itsm.model.User> technicians = userRepository.findByRole("TECHNICIAN");
        List<Map<String, Object>> performance = new ArrayList<>();
        for (com.itsm.model.User tech : technicians) {
            Map<String, Object> perf = new HashMap<>();
            perf.put("technicianId", tech.getId());
            perf.put("technicianName", tech.getName());
            perf.put("totalAssigned", ticketRepository.countByTechnicianId(tech.getId()));
            perf.put("resolved", ticketRepository.countByTechnicianIdAndStatus(tech.getId(), "RESOLVED"));
            perf.put("inProgress", ticketRepository.countByTechnicianIdAndStatus(tech.getId(), "IN_PROGRESS"));
            performance.add(perf);
        }
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/priority-breakdown")
    public ResponseEntity<Map<String, Long>> getPriorityBreakdown() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Long> breakdown = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority() != null ? t.getPriority() : "LOW", Collectors.counting()));
        return ResponseEntity.ok(breakdown);
    }
}
