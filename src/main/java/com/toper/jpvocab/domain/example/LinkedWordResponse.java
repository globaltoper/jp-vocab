package com.toper.jpvocab.domain.example;

public record LinkedWordResponse(
        Long wordId,
        String expression,
        Integer startIndex,
        Integer endIndex
) {
}
