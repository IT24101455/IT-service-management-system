package com.itsm.repository;

import com.itsm.model.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpRepository extends MongoRepository<Otp, String> {
    Optional<Otp> findTopByEmailOrderByExpiryTimeDesc(String email);
    void deleteByEmail(String email);
}
