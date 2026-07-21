package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.common.exception.DictationSentenceNotFoundException;
import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.user.UserRepository;
import com.toper.jpvocab.domain.word.JlptLevel;
import com.toper.jpvocab.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DictationService {

    private final DictationSentenceRepository sentenceRepository;
    private final DictationAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DictationSentenceResponse getRandomSentence(JlptLevel level) {
        DictationSentence sentence = sentenceRepository.findRandomSentence(level == null ? null : level.name());
        if (sentence == null) {
            throw new DictationSentenceNotFoundException();
        }
        return DictationSentenceResponse.from(sentence);
    }

    // 타자 연습용: 딕테이션과 같은 문장 풀에서 뽑지만 정답(읽기)을 그대로 내려준다.
    @Transactional(readOnly = true)
    public TypingPracticeSentenceResponse getRandomSentenceForTyping(JlptLevel level) {
        DictationSentence sentence = sentenceRepository.findRandomSentence(level == null ? null : level.name());
        if (sentence == null) {
            throw new DictationSentenceNotFoundException();
        }
        return TypingPracticeSentenceResponse.from(sentence);
    }

    @Transactional
    public DictationAttemptResponse submitAttempt(Long sentenceId, DictationAttemptRequest request) {
        DictationSentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(DictationSentenceNotFoundException::new);

        int accuracyPercent = TextSimilarity.similarityPercent(request.typedReading(), sentence.getSentenceReading());
        int cpm = calculateCpm(request.typedReading(), request.elapsedMs());

        // 로그인 상태면 기록을 남기고, 비로그인이면 점수만 계산해서 돌려준다(단어 카드 조회와 같은 "인증 선택" 패턴).
        boolean saved = false;
        var currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.isPresent()) {
            User userRef = userRepository.getReferenceById(currentUserId.get());
            attemptRepository.save(new DictationAttempt(userRef, sentence, accuracyPercent, cpm));
            saved = true;
        }

        return new DictationAttemptResponse(
                accuracyPercent, cpm, saved,
                sentence.getSentenceReading(), sentence.getSentenceJp(), sentence.getSentenceMeaning());
    }

    @Transactional(readOnly = true)
    public List<DictationHistoryItemResponse> getHistory(Long userId) {
        return attemptRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DictationHistoryItemResponse::from)
                .toList();
    }

    private int calculateCpm(String typedReading, long elapsedMs) {
        if (elapsedMs <= 0 || typedReading == null || typedReading.isEmpty()) {
            return 0;
        }
        double minutes = elapsedMs / 60000.0;
        return (int) Math.round(typedReading.length() / minutes);
    }
}
