package com.civicPulse.civicPulse_backend.dto;

public class LeaderboardResponse {

    private Integer rank;
    private String name;
    private Integer reputationPoints;
    private String city;

    public LeaderboardResponse(Integer rank, String name, Integer reputationPoints, String city) {
        this.rank = rank;
        this.name = name;
        this.reputationPoints = reputationPoints;
        this.city = city;
    }

    public Integer getRank() { return rank; }
    public String getName() { return name; }
    public Integer getReputationPoints() { return reputationPoints; }
    public String getCity() { return city; }
}