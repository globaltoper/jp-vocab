package com.toper.jpvocab.domain.word;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<Word> findByLevel(JlptLevel level, Pageable pageable);

    /**
     * 표제어/후리가나/뜻 어느 쪽으로 검색해도 걸리게 한다.
     * 「食」로 검색하면 食べる가, 「たべ」로도, 「먹」으로도 찾을 수 있어야 하기 때문이다.
     *
     * level 유무를 하나의 쿼리에서 `:level IS NULL OR ...` 로 처리하지 않고 메서드를 나눈 이유:
     * JPQL에 enum 파라미터를 null로 넘기면 Hibernate가 타입을 추론하지 못해 실패할 수 있다.
     */
    @Query("""
            SELECT w FROM Word w
            WHERE LOWER(w.expression) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(w.furigana)   LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(w.meaning)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Word> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT w FROM Word w
            WHERE w.level = :level
              AND (LOWER(w.expression) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(w.furigana) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(w.meaning)  LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Word> searchByLevel(@Param("level") JlptLevel level,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    @Query(value = "SELECT * FROM words WHERE :level IS NULL OR level = :level ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Word findRandomWord(@Param("level") String level);
}
