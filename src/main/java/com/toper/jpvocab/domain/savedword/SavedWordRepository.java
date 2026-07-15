package com.toper.jpvocab.domain.savedword;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedWordRepository extends JpaRepository<SavedWord, Long> {

    boolean existsByUserIdAndWordId(Long userId, Long wordId);

    Optional<SavedWord> findByUserIdAndWordId(Long userId, Long wordId);

    List<SavedWord> findByUserIdOrderBySavedAtDesc(Long userId);

    List<SavedWord> findByUserIdAndWordIdIn(Long userId, List<Long> wordIds);
}
