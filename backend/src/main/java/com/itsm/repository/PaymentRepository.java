package com.itsm.repository;

import com.itsm.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByUserId(String userId);
    Optional<Payment> findByTicketId(String ticketId);
    List<Payment> findByStatus(String status);
    Optional<Payment> findByInvoiceNumber(String invoiceNumber);
}
