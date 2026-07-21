package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.domain.word.JlptLevel;

public record SignupResponse(
        Long id,
        String username,
        String email,
        JlptLevel targetLevel,
        JlptLevel currentLevel,
        Integer dailyGoalCount
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getTargetLevel(), user.getCurrentLevel(), user.getDailyGoalCount());
    }
}
