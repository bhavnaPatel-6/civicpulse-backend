package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.LeaderboardResponse;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<LeaderboardResponse> getLeaderboard() {

        List<User> topUsers = userRepository.findTop10ByOrderByReputationPointsDesc();

        return IntStream.range(0, topUsers.size())
                .mapToObj(i -> new LeaderboardResponse(
                        i + 1,
                        topUsers.get(i).getName(),
                        topUsers.get(i).getReputationPoints(),
                        topUsers.get(i).getCity()
                ))
                .collect(Collectors.toList());
    }
}