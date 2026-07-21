package com.toper.jpvocab.domain.dictation;

// correctReading은 채점이 끝난 뒤에만 내려준다 - 이제는 정답을 보여줘도 되는 시점이라
// 프런트에서 "정답: ..." 형태로 사용자가 직접 비교해볼 수 있게 한다.
public record DictationAttemptResponse(
        int accuracyPercent,
        int cpm,
        boolean saved,
        String correctReading,
        String sentenceJp,
        String sentenceMeaning
) {
}
