package com.toper.jpvocab.domain.word;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<Word> findByLevel(JlptLevel level, Pageable pageable);

    @Query(value = "SELECT * FROM words WHERE :level IS NULL OR level = :level ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Word findRandomWord(@Param("level") String level);
}
