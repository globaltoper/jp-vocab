package com.toper.jpvocab.domain.user;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
