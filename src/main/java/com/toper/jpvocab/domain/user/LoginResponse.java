package com.toper.jpvocab.domain.user;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
