package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.domain.word.JlptLevel;

// 딕테이션(DictationSentenceResponse)과 같은 문장 풀을 쓰지만, 타자 연습은 "듣고 맞히기"가 아니라
// "보고 빨리 정확하게 치기"이므로 정답(sentenceReading)을 처음부터 그대로 보여준다.
public record TypingPracticeSentenceResponse(
        Long id,
        String sentenceJp,
        String sentenceReading,
        String sentenceMeaning,
        JlptLevel level
) {
    public static TypingPracticeSentenceResponse from(DictationSentence sentence) {
        return new TypingPracticeSentenceResponse(
                sentence.getId(), sentence.getSentenceJp(), sentence.getSentenceReading(),
                sentence.getSentenceMeaning(), sentence.getLevel());
    }
}
