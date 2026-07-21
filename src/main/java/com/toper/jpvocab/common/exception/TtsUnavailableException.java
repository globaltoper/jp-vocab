package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

// VOICEVOX 엔진이 꺼져있거나 연결이 안 될 때. 503으로 응답해서 프런트가
// "고품질 음성 실패 -> 브라우저 기본 TTS로 자동 전환"을 판단할 수 있게 한다.
public class TtsUnavailableException extends ApiException {
    public TtsUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "TTS_UNAVAILABLE", "음성 합성 서버에 연결할 수 없습니다.");
    }
}
