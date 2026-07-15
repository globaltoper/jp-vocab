package com.toper.jpvocab.domain.savedword;

import jakarta.validation.constraints.NotNull;

public record SaveWordRequest(
        @NotNull(message = "wordId는 필수입니다.") Long wordId
) {
}
