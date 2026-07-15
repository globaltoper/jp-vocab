package com.toper.jpvocab.domain.word;

import com.toper.jpvocab.domain.example.ExampleResponse;
import java.util.List;

public record WordDetailResponse(
        Long id,
        String expression,
        String furigana,
        String meaning,
        JlptLevel level,
        String partOfSpeech,
        boolean isSaved,
        List<ExampleResponse> examples
) {
    public static WordDetailResponse of(Word word, boolean isSaved, List<ExampleResponse> examples) {
        return new WordDetailResponse(
                word.getId(), word.getExpression(), word.getFurigana(), word.getMeaning(),
                word.getLevel(), word.getPartOfSpeech(), isSaved, examples);
    }
}
