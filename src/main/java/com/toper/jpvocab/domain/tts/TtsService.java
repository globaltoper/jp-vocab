package com.toper.jpvocab.domain.tts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.toper.jpvocab.common.exception.TtsUnavailableException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * VOICEVOX 엔진(별도 프로세스/서버)에 텍스트를 보내서 음성(wav)을 받아오는 프록시.
 *
 * 왜 프런트에서 VOICEVOX를 직접 호출하지 않는가:
 * - VOICEVOX 엔진에는 인증/CORS가 없어서 브라우저에서 바로 부르면 아무나 우리 엔진 리소스를 쓸 수 있고,
 *   배포 환경에서 VOICEVOX 서버는 외부에 노출하지 않고 백엔드 내부망에서만 접근하게 만드는 게 안전하다.
 * - speedScale 같은 값 조작이나 speaker id 같은 설정을 서버가 일괄 관리할 수 있다.
 *
 * VOICEVOX API는 2단계다: /audio_query로 텍스트를 분석한 "발음 설계도"를 받고,
 * 그 설계도의 speedScale 값을 원하는 속도로 바꿔서 /synthesis에 다시 보내면 실제 wav가 나온다.
 */
@Service
public class TtsService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final int speakerId;

    public TtsService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${voicevox.base-url}") String baseUrl,
            @Value("${voicevox.speaker-id}") int speakerId) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.speakerId = speakerId;
    }

    public byte[] synthesize(String text, double speedScale) {
        try {
            String audioQueryJson = restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/audio_query")
                            .queryParam("text", text)
                            .queryParam("speaker", speakerId)
                            .build())
                    .retrieve()
                    .body(String.class);

            JsonNode audioQuery = objectMapper.readTree(audioQueryJson);
            if (!(audioQuery instanceof ObjectNode audioQueryObject)) {
                throw new TtsUnavailableException();
            }
            audioQueryObject.put("speedScale", speedScale);

            byte[] wav = restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/synthesis")
                            .queryParam("speaker", speakerId)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(audioQueryObject.toString())
                    .retrieve()
                    .body(byte[].class);

            if (wav == null) {
                throw new TtsUnavailableException();
            }
            return wav;
        } catch (RestClientException | IOException ex) {
            // VOICEVOX 엔진이 꺼져있거나(연결 실패) 응답 형식이 이상한 경우 전부 여기로 모인다.
            // 503으로 던지면 프런트가 "고품질 TTS 실패 -> 브라우저 기본 음성으로 폴백"을 할 수 있다.
            throw new TtsUnavailableException();
        }
    }
}
