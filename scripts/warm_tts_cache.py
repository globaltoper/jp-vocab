#!/usr/bin/env python3
"""
DB에 고정으로 들어있는 단어/예문/딕테이션 문장 전부에 대해 VOICEVOX 음성을 미리 만들어
src/main/resources/tts-cache 에 캐시 파일로 채워두는 스크립트.

왜 필요한가:
  이 앱에서 TTS로 말하는 문장은 거의 다 data.sql에 고정으로 들어있는 콘텐츠라서, 매 요청마다
  VOICEVOX에 실시간으로 합성을 요청할 필요가 없다. 이 스크립트로 한 번 미리 다 만들어서
  git에 커밋해두면, 배포된 서버는 VOICEVOX 없이도(또는 느려도) 즉시 음성을 서빙할 수 있다.

사용법:
  1) 로컬 VOICEVOX 도커를 켠다 (README.md의 "발음(TTS)" 섹션 참고).
  2) 캐시가 바로 src/main/resources/tts-cache 에 쌓이도록 지정해서 백엔드를 켠다:
       TTS_CACHE_DIR=src/main/resources/tts-cache ./gradlew bootRun
  3) 이 스크립트를 실행한다 (프로젝트 루트 어디서 실행해도 상관없다):
       python3 scripts/warm_tts_cache.py
  4) 새로 생긴 .wav 파일들을 git에 커밋한다:
       git add src/main/resources/tts-cache
       git commit -m "chore: warm up tts cache"

이미 캐시 파일이 있는 조합은 건너뛰므로, 중간에 실패하거나 멈춰도 다시 실행하면 이어서 진행된다.
단어/딕테이션 문장을 새로 추가한 뒤에도 이 스크립트를 다시 돌리면 새로 추가된 것만 생성된다.
"""

import hashlib
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DATA_SQL = PROJECT_ROOT / "src/main/resources/data.sql"
CACHE_DIR = PROJECT_ROOT / "src/main/resources/tts-cache"
BACKEND_URL = "http://localhost:8080/api/tts/speak"

# 백엔드 domain/tts/Voice.java 의 enum 이름 및 speaker id와 반드시 일치해야 한다.
VOICES = {
    "FEMALE": 2,  # 四国めたん
    "MALE": 11,  # 玄野武宏
}


def parse_sql_values(tuple_str: str) -> list[str]:
    """VALUES(...) 안의 한 튜플을 SQL 문자열 이스케이프('' -> ')를 고려해 값 리스트로 분해한다."""
    values: list[str] = []
    i, n = 0, len(tuple_str)
    while i < n:
        while i < n and tuple_str[i] in " \t\n":
            i += 1
        if i >= n:
            break
        if tuple_str[i] == "'":
            i += 1
            buf = []
            while i < n:
                if tuple_str[i] == "'":
                    if i + 1 < n and tuple_str[i + 1] == "'":
                        buf.append("'")
                        i += 2
                        continue
                    i += 1
                    break
                buf.append(tuple_str[i])
                i += 1
            values.append("".join(buf))
        else:
            j = i
            while j < n and tuple_str[j] != ",":
                j += 1
            values.append(tuple_str[i:j].strip())
            i = j
        while i < n and tuple_str[i] in " \t\n":
            i += 1
        if i < n and tuple_str[i] == ",":
            i += 1
    return values


def extract_texts(table: str, value_index: int) -> list[str]:
    """data.sql에서 특정 테이블의 INSERT 문들을 찾아, 지정한 인덱스의 값(문장/단어 텍스트)만 뽑는다."""
    prefix = f"INSERT IGNORE INTO {table} ("
    values_marker = ") VALUES ("
    texts = []
    for line in DATA_SQL.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line.startswith(prefix):
            continue
        idx = line.index(values_marker)
        tuple_str = line[idx + len(values_marker):]
        if tuple_str.endswith(");"):
            tuple_str = tuple_str[:-2]
        values = parse_sql_values(tuple_str)
        texts.append(values[value_index])
    return texts


def cache_key(text: str, speaker_id: int, speed_scale: float) -> str:
    """백엔드 TtsService.cacheKey()와 정확히 같은 방식으로 계산해야 같은 파일을 가리킨다."""
    normalized_speed = f"{speed_scale:.2f}"
    raw = f"{text} {speaker_id} {normalized_speed}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def request_and_cache(text: str, speed_scale: float, voice_name: str, speaker_id: int) -> bool:
    """반환값: 실제로 새로 요청을 보냈으면 True, 이미 캐시 파일이 있어서 건너뛰었으면 False."""
    key = cache_key(text, speaker_id, speed_scale)
    if (CACHE_DIR / f"{key}.wav").exists():
        return False

    body = json.dumps({"text": text, "speedScale": speed_scale, "voice": voice_name}).encode("utf-8")
    req = urllib.request.Request(
        BACKEND_URL, data=body, headers={"Content-Type": "application/json"}, method="POST"
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        resp.read()  # 백엔드가 TTS_CACHE_DIR에 알아서 저장한다 - 응답 바이트 자체는 필요 없다.
    return True


def main() -> None:
    if not DATA_SQL.exists():
        print(f"data.sql을 찾을 수 없습니다: {DATA_SQL}", file=sys.stderr)
        sys.exit(1)

    CACHE_DIR.mkdir(parents=True, exist_ok=True)

    words = extract_texts("words", 1)
    examples = extract_texts("example_sentences", 2)
    dictation = extract_texts("dictation_sentences", 1)
    print(f"단어 {len(words)}개, 예문 {len(examples)}개, 딕테이션 문장 {len(dictation)}개 발견")

    jobs: list[tuple[str, float, str]] = []
    for text in words:
        for voice_name in VOICES:
            jobs.append((text, 1.0, voice_name))
    for text in examples:
        for voice_name in VOICES:
            jobs.append((text, 1.0, voice_name))
    for text in dictation:
        for voice_name in VOICES:
            jobs.append((text, 1.0, voice_name))
            jobs.append((text, 0.7, voice_name))  # 딕테이션의 "천천히 듣기"용 속도

    total = len(jobs)
    created = skipped = failed = 0

    for i, (text, speed, voice_name) in enumerate(jobs, start=1):
        speaker_id = VOICES[voice_name]
        try:
            if request_and_cache(text, speed, voice_name, speaker_id):
                created += 1
            else:
                skipped += 1
        except urllib.error.URLError as e:
            failed += 1
            print(f"  [실패] '{text[:20]}...' ({voice_name}, {speed}): {e}", file=sys.stderr)

        if i % 20 == 0 or i == total:
            print(f"진행 {i}/{total} (신규 생성 {created} · 이미 있음 {skipped} · 실패 {failed})")

    print("완료.")
    if failed:
        print(
            f"{failed}개 실패했습니다. 백엔드(./gradlew bootRun)와 VOICEVOX 도커가 켜져 있는지 "
            f"확인한 뒤 스크립트를 다시 실행하세요 - 이미 성공한 건 건너뛰고 실패한 것만 재시도합니다."
        )


if __name__ == "__main__":
    main()
