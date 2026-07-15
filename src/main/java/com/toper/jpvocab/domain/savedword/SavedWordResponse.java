package com.toper.jpvocab.domain.savedword;

import java.time.LocalDateTime;

public record SavedWordResponse(
        Long id,
        Long wordId,
        String expression,
        LocalDateTime savedAt
) {
    public static SavedWordResponse from(SavedWord savedWord) {
        return new SavedWordResponse(
                savedWord.getId(),
                savedWord.getWord().getId(),
                savedWord.getWord().getExpression(),
                savedWord.getSavedAt());
    }
}
