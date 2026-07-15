package com.toper.jpvocab.domain.review;

import jakarta.validation.constraints.NotNull;

public record ReviewResultRequest(
        @NotNull(message = "remembered는 필수입니다.") Boolean remembered
) {
}
