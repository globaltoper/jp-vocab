package com.toper.jpvocab.domain.example;

import java.util.List;

public record ExampleResponse(
        Long id,
        String sentenceJp,
        String sentenceReading,
        String sentenceMeaning,
        List<LinkedWordResponse> linkedWords
) {
}
