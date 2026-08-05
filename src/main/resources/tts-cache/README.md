# tts-cache

이 폴더는 `scripts/warm_tts_cache.py`로 미리 생성해둔 VOICEVOX 음성 파일(`.m4a`)이 저장되는 곳입니다.

- 이 폴더에 있는 `.m4a` 파일들은 빌드 시 jar 안에 그대로 포함되어(classpath 리소스), 배포된 서버가
  VOICEVOX 없이도(또는 VOICEVOX가 응답하지 않아도) 즉시 음성을 서빙할 수 있게 해줍니다.
- 파일명은 사람이 알아볼 수 있는 이름이 아니라 `(문장 텍스트, 목소리, 속도)`를 SHA-256으로 해시한
  값입니다(`TtsService.cacheKey` 참고) — 직접 만들거나 이름을 바꾸지 마세요.
- 새 단어/예문/딕테이션 문장을 추가했다면, 아래처럼 이 폴더를 다시 채운 뒤 커밋하세요.

```bash
# 0) ffmpeg 필요 (WAV -> AAC 변환용).  macOS: brew install ffmpeg
# 1) 로컬 VOICEVOX 실행 (README.md "발음(TTS)" 섹션 참고)
# 2) 백엔드 실행. TTS_CACHE_DIR은 건드리지 말 것 (아래 주의 참고)
./gradlew bootRun

# 3) 다른 터미널에서 warm-up 스크립트 실행 (전체 콘텐츠 × 여성/남성 음성 전부 미리 생성)
python3 scripts/warm_tts_cache.py

# 4) 새로 생긴 .m4a 파일들을 git에 커밋
git add src/main/resources/tts-cache
git commit -m "chore: warm up tts cache"
```

## 왜 WAV가 아니라 AAC(.m4a)인가

VOICEVOX 원본은 무압축 WAV라 같은 음성이 7배 넘게 무겁습니다. 단어 수를 크게 늘릴 계획이라
저장소와 배포 이미지가 감당이 안 되기 때문에, 커밋 전에 AAC 48kbps 모노로 변환해서 저장합니다.
(실측: 1,288개 기준 186MB → 29MB)

압축률만 보면 Opus가 더 좋지만, Safari가 Ogg Opus를 18.4(2025년 3월)부터야 지원하고 그마저
불안정합니다. 모든 iOS 버전에서 확실히 재생되는 AAC를 택했습니다.

런타임 캐시(`TTS_CACHE_DIR`)에 쌓이는 파일은 VOICEVOX 원본 그대로인 `.wav`입니다 — 서버에서
변환하려면 배포 이미지에 ffmpeg을 넣어야 해서, 실시간 합성 폴백 경로는 변환 없이 내보냅니다.

## 주의: TTS_CACHE_DIR을 이 폴더로 지정하지 마세요

warm-up 스크립트가 백엔드에 합성을 요청하면, 백엔드는 그 결과(원본 WAV)를 `TTS_CACHE_DIR`에
따로 저장합니다. 이 폴더를 지정하면 스크립트가 만드는 `.m4a`와 **내용이 같은 `.wav`가 나란히
쌓입니다.** 실제로 이 문제로 171MB(파일 5,098개)가 낭비된 적이 있습니다.

기본값(`/tmp/jpvocab-tts-cache`)을 그대로 두세요. 혹시 실수로 들어와도 커밋되지 않도록
`.gitignore`에 `src/main/resources/tts-cache/*.wav`를 넣어두었습니다.
