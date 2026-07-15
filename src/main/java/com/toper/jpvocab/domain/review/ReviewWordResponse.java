package com.toper.jpvocab.domain.review;

import com.toper.jpvocab.domain.word.JlptLevel;
import java.time.LocalDateTime;

public record ReviewWordResponse(
        Long wordId,
        String expression,
        String furigana,
        String meaning,
        JlptLevel level,
        String partOfSpeech,
        int boxLevel,
        LocalDateTime nextReviewAt
) {
    public static ReviewWordResponse from(ReviewSchedule schedule) {
        var word = schedule.getWord();
        return new ReviewWordResponse(
                word.getId(), word.getExpression(), word.getFurigana(), word.getMeaning(),
                word.getLevel(), word.getPartOfSpeech(), schedule.getBoxLevel(), schedule.getNextReviewAt());
    }
}
