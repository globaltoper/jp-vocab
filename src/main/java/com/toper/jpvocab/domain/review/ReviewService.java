package com.toper.jpvocab.domain.review;

import com.toper.jpvocab.common.exception.ReviewNotFoundException;
import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.word.Word;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewScheduleRepository reviewScheduleRepository;

    /**
     * 단어를 저장할 때 호출된다. 이미 스케줄이 있으면(과거에 저장했다가 삭제 후 재저장 등) 건드리지 않는다.
     */
    @Transactional
    public void createScheduleIfAbsent(User user, Word word) {
        reviewScheduleRepository.findByUserIdAndWordId(user.getId(), word.getId())
                .orElseGet(() -> reviewScheduleRepository.save(new ReviewSchedule(user, word)));
    }

    /**
     * 저장 단어를 삭제할 때 호출된다.
     */
    @Transactional
    public void deleteSchedule(Long userId, Long wordId) {
        reviewScheduleRepository.deleteByUserIdAndWordId(userId, wordId);
    }

    @Transactional(readOnly = true)
    public List<ReviewWordResponse> getDueReviews(Long userId) {
        return reviewScheduleRepository
                .findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(userId, LocalDateTime.now())
                .stream()
                .map(ReviewWordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewDueCountResponse getDueCount(Long userId) {
        long count = reviewScheduleRepository.countByUserIdAndNextReviewAtLessThanEqual(userId, LocalDateTime.now());
        return new ReviewDueCountResponse(count);
    }

    @Transactional
    public ReviewWordResponse submitResult(Long userId, Long wordId, boolean remembered) {
        ReviewSchedule schedule = reviewScheduleRepository.findByUserIdAndWordId(userId, wordId)
                .orElseThrow(() -> new ReviewNotFoundException(wordId));

        if (remembered) {
            schedule.markRemembered();
        } else {
            schedule.markForgotten();
        }

        return ReviewWordResponse.from(schedule);
    }
}
