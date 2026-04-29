package com.itsm.controller;

import com.itsm.model.Payment;
import com.itsm.repository.PaymentRepository;
import com.itsm.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(paymentRepository.findByUserId(userId));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<Payment> getPaymentByTicket(@PathVariable String ticketId) {
        return paymentRepository.findByTicketId(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<Payment> getByInvoice(@PathVariable String invoiceNumber) {
        return paymentRepository.findByInvoiceNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        String invoiceNumber = "INV-" + System.currentTimeMillis();
        payment.setInvoiceNumber(invoiceNumber);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(paymentRepository.save(payment));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Payment> markAsPaid(@PathVariable String id, @RequestBody Payment body) {
        return paymentRepository.findById(id).map(payment -> {
            payment.setStatus("PAID");
            payment.setPaymentMethod(body.getPaymentMethod());
            payment.setPaidAt(LocalDateTime.now());
            return ResponseEntity.ok(paymentRepository.save(payment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable String id) {
        paymentRepository.deleteById(id);
        return ResponseEntity.ok("Payment deleted");
    }
}
