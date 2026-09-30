package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.entity.SensitiveZone;
import com.civicPulse.civicPulse_backend.repository.SensitiveZoneRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sensitive-zones")
@PreAuthorize("hasRole('ADMIN')")   // apne security config ke hisaab se adjust karo
public class SensitiveZoneAdminController {

    private final SensitiveZoneRepository repo;

    public SensitiveZoneAdminController(SensitiveZoneRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public SensitiveZone create(@RequestBody SensitiveZone zone) {
        zone.setId(null);
        return repo.save(zone);
    }

    @GetMapping
    public List<SensitiveZone> all() {
        return repo.findAll();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<SensitiveZone> toggle(@PathVariable Long id) {
        return repo.findById(id).map(z -> {
            z.setActive(!z.isActive());
            return ResponseEntity.ok(repo.save(z));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}