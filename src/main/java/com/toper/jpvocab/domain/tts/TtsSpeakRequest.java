package com.toper.jpvocab.domain.tts;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record TtsSpeakRequest(
        @NotBlank(message = "text는 필수입니다.") String text,
        @DecimalMin(value = "0.5", message = "speedScale은 0.5 이상이어야 합니다.")
        @DecimalMax(value = "2.0", message = "speedScale은 2.0 이하여야 합니다.")
        Double speedScale,
        Voice voice
) {
    // speedScale을 안 보내면 1.0(기본 속도)으로 취급한다.
    public double resolvedSpeedScale() {
        return speedScale != null ? speedScale : 1.0;
    }

    // voice를 안 보내면 여성(四国めたん)을 기본값으로 취급한다.
    public Voice resolvedVoice() {
        return voice != null ? voice : Voice.FEMALE;
    }
}
