package com.toper.jpvocab.domain.dictation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DictationAttemptRequest(
        @NotNull(message = "typedReading은 필수입니다.") String typedReading,
        @NotNull(message = "elapsedMs는 필수입니다.")
        @PositiveOrZero(message = "elapsedMs는 0 이상이어야 합니다.")
        Long elapsedMs
) {
}
