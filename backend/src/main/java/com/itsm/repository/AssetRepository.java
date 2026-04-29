package com.itsm.repository;

import com.itsm.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AssetRepository extends MongoRepository<Asset, String> {
    List<Asset> findByStatus(String status);
    List<Asset> findByAssignedToUserId(String userId);
    List<Asset> findByType(String type);
}
