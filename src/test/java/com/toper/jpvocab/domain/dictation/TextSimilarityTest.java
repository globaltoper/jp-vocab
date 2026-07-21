package com.toper.jpvocab.domain.dictation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TextSimilarity는 package-private이라 같은 패키지(com.toper.jpvocab.domain.dictation)의
 * 테스트 클래스에서만 접근할 수 있다. 딕테이션 채점의 핵심 로직이라 순수 단위 테스트로
 * 커버한다(스프링 컨텍스트/DB 불필요 - 제일 빠르고 신뢰도 높은 테스트).
 */
class TextSimilarityTest {

    @Test
    @DisplayName("완전히 같은 문자열이면 100점")
    void exactMatch_returns100() {
        int result = TextSimilarity.similarityPercent("きょうはてんきがいいですね", "きょうはてんきがいいですね");
        assertThat(result).isEqualTo(100);
    }

    @Test
    @DisplayName("공백/구두점 차이는 무시하고 100점 처리한다")
    void punctuationAndWhitespaceIgnored() {
        int result = TextSimilarity.similarityPercent("きょうは てんきが いいですね", "きょうはてんきがいいですね。");
        assertThat(result).isEqualTo(100);
    }

    @Test
    @DisplayName("한 글자만 다르면 100점보다는 낮지만 0점은 아니다(부분 점수)")
    void oneCharacterDiff_partialScore() {
        int result = TextSimilarity.similarityPercent("きょうはてんきがわるいですね", "きょうはてんきがいいですね");
        assertThat(result).isBetween(1, 99);
    }

    @Test
    @DisplayName("정답이 비어있지 않은데 입력이 완전히 비어있으면 0점")
    void emptyTyped_returns0() {
        int result = TextSimilarity.similarityPercent("", "きょうはてんきがいいですね");
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("정답과 입력이 둘 다 비어있으면 100점 처리(둘 다 빈 문자열이면 일치로 간주)")
    void bothEmpty_returns100() {
        int result = TextSimilarity.similarityPercent("", "");
        assertThat(result).isEqualTo(100);
    }

    @Test
    @DisplayName("null 입력은 빈 문자열처럼 취급되어 예외를 던지지 않는다")
    void nullTyped_doesNotThrow() {
        int result = TextSimilarity.similarityPercent(null, "きょうは");
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("완전히 다른 문자열이면 0점에 가깝다")
    void completelyDifferent_lowScore() {
        int result = TextSimilarity.similarityPercent("あいうえお", "わたしはがくせいです");
        assertThat(result).isLessThan(30);
    }

    @Test
    @DisplayName("normalize는 공백과 구두점(。、！？!?.,)을 전부 제거한다")
    void normalize_stripsWhitespaceAndPunctuation() {
        String normalized = TextSimilarity.normalize("これは 、 テスト です !? 。");
        assertThat(normalized).doesNotContain(" ", "、", "。", "!", "?");
    }
}
