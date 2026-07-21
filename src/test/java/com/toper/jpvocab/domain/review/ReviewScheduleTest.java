package com.toper.jpvocab.domain.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.word.JlptLevel;
import com.toper.jpvocab.domain.word.Word;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 라이트너 박스 스케줄링 로직의 순수 단위 테스트. DB/스프링 컨텍스트 없이
 * 엔티티 메서드(markRemembered/markForgotten)만 직접 검증한다.
 */
class ReviewScheduleTest {

    private User user;
    private Word word;

    @BeforeEach
    void setUp() {
        user = User.builder().username("tester").password("encoded").email("t@test.com").build();
        word = new Word("食べる", "たべる", "먹다", JlptLevel.N5, "동사");
    }

    @Test
    @DisplayName("새로 생성된 스케줄은 box 1에서 시작한다")
    void newSchedule_startsAtBoxOne() {
        ReviewSchedule schedule = new ReviewSchedule(user, word);
        assertThat(schedule.getBoxLevel()).isEqualTo(ReviewSchedule.MIN_BOX_LEVEL);
    }

    @Test
    @DisplayName("기억했다고 표시하면 박스가 한 단계 올라가고 다음 복습일이 미래로 밀린다")
    void markRemembered_incrementsBoxAndPushesNextReview() {
        ReviewSchedule schedule = new ReviewSchedule(user, word);
        LocalDateTime before = schedule.getNextReviewAt();

        schedule.markRemembered();

        assertThat(schedule.getBoxLevel()).isEqualTo(2);
        assertThat(schedule.getNextReviewAt()).isAfter(before);
        assertThat(schedule.getLastReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("박스 5(최고 단계)에서 기억했다고 표시해도 5를 넘지 않는다")
    void markRemembered_capsAtMaxBoxLevel() {
        ReviewSchedule schedule = new ReviewSchedule(user, word);
        for (int i = 0; i < 10; i++) {
            schedule.markRemembered();
        }
        assertThat(schedule.getBoxLevel()).isEqualTo(ReviewSchedule.MAX_BOX_LEVEL);
    }

    @Test
    @DisplayName("못 외웠다고 표시하면 박스가 무조건 1로 초기화된다")
    void markForgotten_resetsToBoxOne() {
        ReviewSchedule schedule = new ReviewSchedule(user, word);
        schedule.markRemembered();
        schedule.markRemembered();
        assertThat(schedule.getBoxLevel()).isEqualTo(3);

        schedule.markForgotten();

        assertThat(schedule.getBoxLevel()).isEqualTo(ReviewSchedule.MIN_BOX_LEVEL);
    }

    @Test
    @DisplayName("못 외웠다고 표시해도 마지막 복습 시각은 기록된다")
    void markForgotten_stillRecordsLastReviewedAt() {
        ReviewSchedule schedule = new ReviewSchedule(user, word);
        schedule.markForgotten();
        assertThat(schedule.getLastReviewedAt()).isNotNull();
    }
}
