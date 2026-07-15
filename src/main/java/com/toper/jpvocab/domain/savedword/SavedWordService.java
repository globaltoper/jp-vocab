package com.toper.jpvocab.domain.savedword;

import com.toper.jpvocab.common.exception.AlreadySavedException;
import com.toper.jpvocab.common.exception.ForbiddenException;
import com.toper.jpvocab.common.exception.SavedWordNotFoundException;
import com.toper.jpvocab.common.exception.WordNotFoundException;
import com.toper.jpvocab.domain.review.ReviewService;
import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.user.UserRepository;
import com.toper.jpvocab.domain.word.Word;
import com.toper.jpvocab.domain.word.WordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedWordService {

    private final SavedWordRepository savedWordRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final ReviewService reviewService;

    @Transactional
    public SavedWordResponse save(Long userId, SaveWordRequest request) {
        Word word = wordRepository.findById(request.wordId())
                .orElseThrow(() -> new WordNotFoundException(request.wordId()));

        if (savedWordRepository.existsByUserIdAndWordId(userId, word.getId())) {
            throw new AlreadySavedException(word.getId());
        }

        User userRef = userRepository.getReferenceById(userId);
        SavedWord saved = savedWordRepository.save(new SavedWord(userRef, word));
        reviewService.createScheduleIfAbsent(userRef, word);
        return SavedWordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SavedWordListItemResponse> getSavedWords(Long userId) {
        return savedWordRepository.findByUserIdOrderBySavedAtDesc(userId).stream()
                .map(SavedWordListItemResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long savedWordId) {
        SavedWord savedWord = savedWordRepository.findById(savedWordId)
                .orElseThrow(() -> new SavedWordNotFoundException(savedWordId));

        if (!savedWord.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인이 저장한 단어만 삭제할 수 있습니다.");
        }

        reviewService.deleteSchedule(userId, savedWord.getWord().getId());
        savedWordRepository.delete(savedWord);
    }
}
