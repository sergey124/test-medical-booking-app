package com.kry.triage.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    /**
     * Returns a care recommendation based on the triage score.
     * Score 5–7  → Chat
     * Score 8–11 → Nurse
     * Score 12–15 → Doctor
     */
    public String recommend(int score) {
        if (score <= 7) return "Chat";
        if (score <= 11) return "Nurse";
        return "Doctor";
    }
}
