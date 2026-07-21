package com.toper.jpvocab.domain.user;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "token은 필수입니다.") String token
) {
}
