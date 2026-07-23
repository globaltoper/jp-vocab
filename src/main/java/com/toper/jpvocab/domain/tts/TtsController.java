package com.toper.jpvocab.domain.tts;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고품질 일본어 TTS(VOICEVOX) API. 단어 카드/예문/딕테이션 등 앱 전체에서 공통으로 쓴다.
 * 비로그인 사용자도 발음을 들을 수 있어야 하므로 permitAll (SecurityConfig 참고).
 */
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Tag(name = "TTS", description = "VOICEVOX 기반 일본어 음성 합성 (인증 불필요). 응답이 audio/wav라 Swagger UI에서는 재생 대신 다운로드로 확인해야 한다.")
public class TtsController {

    private final TtsService ttsService;

    @PostMapping(value = "/speak", produces = "audio/wav")
    public ResponseEntity<byte[]> speak(@Valid @RequestBody TtsSpeakRequest request) {
        byte[] audio = ttsService.synthesize(request.text(), request.resolvedSpeedScale(), request.resolvedVoice());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }
}
