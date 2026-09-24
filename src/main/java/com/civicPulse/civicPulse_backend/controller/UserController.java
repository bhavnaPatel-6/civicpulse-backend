package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.RewardHistoryResponse;
import com.civicPulse.civicPulse_backend.dto.UserResponse;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.UserRepository;
import com.civicPulse.civicPulse_backend.service.ComplaintService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ComplaintService complaintService;

    public UserController(UserRepository userRepository, ComplaintService complaintService) {
        this.userRepository = userRepository;
        this.complaintService = complaintService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCity(),
                user.getWard(),
                user.getRole(),
                user.getDepartment(),
                user.getReputationPoints(),
                user.getCreatedAt()
        );
    }

    // Citizen: apni reward/points history dekho
    @GetMapping("/me/rewards")
    public Page<RewardHistoryResponse> getMyRewards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        return complaintService.getMyRewardHistory(email, pageable);
    }
}