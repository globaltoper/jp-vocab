package com.toper.jpvocab.domain.dictation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictationAttemptRepository extends JpaRepository<DictationAttempt, Long> {

    List<DictationAttempt> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
