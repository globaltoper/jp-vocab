package com.toper.jpvocab.domain.tts;

/**
 * 선택 가능한 음성(성별). VOICEVOX가 제공하는 여러 캐릭터 중 성별이 명확한 둘을 골랐다.
 *
 * - FEMALE: 四国めたん(시코쿠 메탄) ノーマル - VOICEVOX 기본 캐릭터, speaker id 2
 * - MALE: 玄野武宏(쿠로노 타케히로) ノーマル - VOICEVOX 기본 캐릭터, speaker id 11
 *
 * speaker id는 VOICEVOX 엔진의 GET /speakers 로 직접 확인 가능하며, 새 캐릭터가 추가돼도
 * 기존 id는 바뀌지 않는다(공식 정책).
 *
 * 주의: 이 목소리들을 쓰는 이상 "VOICEVOX:四国めたん" / "VOICEVOX:玄野武宏" 식의 크레딧 표시가
 * VOICEVOX 이용약관상 필수다. 프론트엔드 Footer 컴포넌트에 이미 반영돼 있다.
 */
public enum Voice {
    FEMALE(2, "四国めたん"),
    MALE(11, "玄野武宏");

    private final int speakerId;
    private final String displayName;

    Voice(int speakerId, String displayName) {
        this.speakerId = speakerId;
        this.displayName = displayName;
    }

    public int speakerId() {
        return speakerId;
    }

    public String displayName() {
        return displayName;
    }
}
