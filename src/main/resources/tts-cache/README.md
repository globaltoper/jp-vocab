# tts-cache

이 폴더는 `scripts/warm_tts_cache.py`로 미리 생성해둔 VOICEVOX 음성 파일(.wav)이 저장되는 곳입니다.

- 이 폴더에 있는 `.wav` 파일들은 빌드 시 jar 안에 그대로 포함되어(classpath 리소스), 배포된 서버가
  VOICEVOX 없이도(또는 VOICEVOX가 응답하지 않아도) 즉시 음성을 서빙할 수 있게 해줍니다.
- 파일명은 사람이 알아볼 수 있는 이름이 아니라 `(문장 텍스트, 목소리, 속도)`를 SHA-256으로 해시한
  값입니다(`TtsService.cacheKey` 참고) — 직접 만들거나 이름을 바꾸지 마세요.
- 새 단어/예문/딕테이션 문장을 추가했다면, 아래처럼 이 폴더를 다시 채운 뒤 커밋하세요.

```bash
# 1) 로컬 VOICEVOX 실행 (README.md "발음(TTS)" 섹션 참고)
# 2) 캐시 파일이 바로 이 폴더에 쓰이도록 지정해서 백엔드 실행
TTS_CACHE_DIR=src/main/resources/tts-cache ./gradlew bootRun

# 3) 다른 터미널에서 warm-up 스크립트 실행 (전체 콘텐츠 × 여성/남성 음성 전부 미리 생성)
python3 scripts/warm_tts_cache.py

# 4) 새로 생긴 .wav 파일들을 git에 커밋
git add src/main/resources/tts-cache
git commit -m "chore: warm up tts cache"
```
