package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.domain.word.JlptLevel;

// sentenceReading(정답)은 절대 포함하지 않는다 - 이 응답은 채점 전에 프런트로 나가므로
// 개발자 도구 네트워크 탭만 봐도 정답이 그대로 노출되는 문제가 생긴다.
// sentenceJp는 TTS(말하기) 재생용으로만 쓰고, 화면에는 채점 전까지 보여주지 않는다.
public record DictationSentenceResponse(
        Long id,
        String sentenceJp,
        String sentenceMeaning,
        JlptLevel level
) {
    public static DictationSentenceResponse from(DictationSentence sentence) {
        return new DictationSentenceResponse(
                sentence.getId(), sentence.getSentenceJp(), sentence.getSentenceMeaning(), sentence.getLevel());
    }
}
