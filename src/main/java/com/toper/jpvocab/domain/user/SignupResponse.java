package com.toper.jpvocab.domain.user;

public record SignupResponse(Long id, String username, String email) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
