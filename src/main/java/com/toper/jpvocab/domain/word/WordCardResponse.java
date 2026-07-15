package com.toper.jpvocab.domain.word;

public record WordCardResponse(
        Long id,
        String expression,
        String furigana,
        String meaning,
        JlptLevel level,
        String partOfSpeech,
        boolean isSaved
) {
    public static WordCardResponse of(Word word, boolean isSaved) {
        return new WordCardResponse(
                word.getId(), word.getExpression(), word.getFurigana(), word.getMeaning(),
                word.getLevel(), word.getPartOfSpeech(), isSaved);
    }
}
