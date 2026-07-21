package com.toper.jpvocab.domain.dictation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DictationSentenceRepository extends JpaRepository<DictationSentence, Long> {

    @Query(value = "SELECT * FROM dictation_sentences WHERE :level IS NULL OR level = :level ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    DictationSentence findRandomSentence(@Param("level") String level);
}
