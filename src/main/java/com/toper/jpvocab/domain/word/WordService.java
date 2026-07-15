package com.toper.jpvocab.domain.word;

import com.toper.jpvocab.common.exception.WordNotFoundException;
import com.toper.jpvocab.domain.example.ExampleResponse;
import com.toper.jpvocab.domain.example.ExampleService;
import com.toper.jpvocab.domain.savedword.SavedWordRepository;
import com.toper.jpvocab.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final SavedWordRepository savedWordRepository;
    private final ExampleService exampleService;

    @Transactional(readOnly = true)
    public WordCardResponse getRandomWord(JlptLevel level) {
        Word word = wordRepository.findRandomWord(level == null ? null : level.name());
        if (word == null) {
            throw new WordNotFoundException("조건에 맞는 단어가 없습니다.");
        }
        boolean isSaved = isSavedByCurrentUser(word.getId());
        return WordCardResponse.of(word, isSaved);
    }

    @Transactional(readOnly = true)
    public WordDetailResponse getWordDetail(Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new WordNotFoundException(wordId));

        boolean isSaved = isSavedByCurrentUser(word.getId());
        List<ExampleResponse> examples = exampleService.getExamplesForWord(word);
        return WordDetailResponse.of(word, isSaved, examples);
    }

    @Transactional(readOnly = true)
    public WordPageResponse getWords(JlptLevel level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Word> wordPage = (level == null)
                ? wordRepository.findAll(pageable)
                : wordRepository.findByLevel(level, pageable);
        return WordPageResponse.from(wordPage);
    }

    private boolean isSavedByCurrentUser(Long wordId) {
        return SecurityUtils.getCurrentUserId()
                .map(userId -> savedWordRepository.existsByUserIdAndWordId(userId, wordId))
                .orElse(false);
    }
}
