package com.itsm.controller;

import com.itsm.model.Asset;
import com.itsm.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetRepository assetRepository;

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable String id) {
        return assetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Asset>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(assetRepository.findByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Asset>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(assetRepository.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(assetRepository.save(asset));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable String id, @RequestBody Asset updated) {
        return assetRepository.findById(id).map(asset -> {
            if (updated.getName() != null) asset.setName(updated.getName());
            if (updated.getType() != null) asset.setType(updated.getType());
            if (updated.getStatus() != null) asset.setStatus(updated.getStatus());
            if (updated.getAssignedToUserId() != null) asset.setAssignedToUserId(updated.getAssignedToUserId());
            if (updated.getAssignedToUserName() != null) asset.setAssignedToUserName(updated.getAssignedToUserName());
            if (updated.getLocation() != null) asset.setLocation(updated.getLocation());
            if (updated.getNotes() != null) asset.setNotes(updated.getNotes());
            asset.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(assetRepository.save(asset));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable String id) {
        assetRepository.deleteById(id);
        return ResponseEntity.ok("Asset deleted");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
            "total", assetRepository.count(),
            "active", (long) assetRepository.findByStatus("ACTIVE").size(),
            "maintenance", (long) assetRepository.findByStatus("UNDER_MAINTENANCE").size(),
            "inactive", (long) assetRepository.findByStatus("INACTIVE").size()
        ));
    }
}
