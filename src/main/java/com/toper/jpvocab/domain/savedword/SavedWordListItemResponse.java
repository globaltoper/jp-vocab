package com.toper.jpvocab.domain.savedword;

import com.toper.jpvocab.domain.word.JlptLevel;
import java.time.LocalDateTime;

public record SavedWordListItemResponse(
        Long id,
        Long wordId,
        String expression,
        String furigana,
        String meaning,
        JlptLevel level,
        LocalDateTime savedAt
) {
    public static SavedWordListItemResponse from(SavedWord savedWord) {
        var word = savedWord.getWord();
        return new SavedWordListItemResponse(
                savedWord.getId(), word.getId(), word.getExpression(), word.getFurigana(),
                word.getMeaning(), word.getLevel(), savedWord.getSavedAt());
    }
}
