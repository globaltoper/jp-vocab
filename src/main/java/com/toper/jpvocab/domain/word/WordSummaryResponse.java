package com.toper.jpvocab.domain.word;

public record WordSummaryResponse(
        Long id,
        String expression,
        String furigana,
        String meaning,
        JlptLevel level
) {
    public static WordSummaryResponse from(Word word) {
        return new WordSummaryResponse(
                word.getId(), word.getExpression(), word.getFurigana(), word.getMeaning(), word.getLevel());
    }
}
