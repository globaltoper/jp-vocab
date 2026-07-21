package com.toper.jpvocab.domain.dictation;

import java.time.LocalDateTime;

public record DictationHistoryItemResponse(
        Long id,
        String sentenceJp,
        String sentenceMeaning,
        int accuracyPercent,
        int cpm,
        LocalDateTime createdAt
) {
    public static DictationHistoryItemResponse from(DictationAttempt attempt) {
        return new DictationHistoryItemResponse(
                attempt.getId(),
                attempt.getSentence().getSentenceJp(),
                attempt.getSentence().getSentenceMeaning(),
                attempt.getAccuracyPercent(),
                attempt.getCpm(),
                attempt.getCreatedAt());
    }
}
