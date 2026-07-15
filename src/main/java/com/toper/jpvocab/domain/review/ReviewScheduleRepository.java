package com.toper.jpvocab.domain.review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewScheduleRepository extends JpaRepository<ReviewSchedule, Long> {

    Optional<ReviewSchedule> findByUserIdAndWordId(Long userId, Long wordId);

    List<ReviewSchedule> findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(
            Long userId, LocalDateTime now);

    long countByUserIdAndNextReviewAtLessThanEqual(Long userId, LocalDateTime now);

    void deleteByUserIdAndWordId(Long userId, Long wordId);
}
