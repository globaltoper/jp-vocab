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
  2) 백엔드를 켠다. TTS_CACHE_DIR은 기본값(임시 폴더) 그대로 두는 게 좋다:
       ./gradlew bootRun
     주의: 여기에 src/main/resources/tts-cache 를 지정하면 안 된다. 백엔드는 실시간 합성
     결과를 원본 WAV로 그 폴더에 쓰는데, 이 스크립트가 만드는 m4a와 내용이 같은 중복이
     수천 개 쌓여서 저장소가 몇 배로 불어난다(실제로 171MB가 낭비된 적이 있다).
  3) 이 스크립트를 실행한다 (프로젝트 루트 어디서 실행해도 상관없다):
       python3 scripts/warm_tts_cache.py
  4) 새로 생긴 .m4a 파일들을 git에 커밋한다:
       git add src/main/resources/tts-cache
       git commit -m "chore: warm up tts cache"

필요 조건: ffmpeg (VOICEVOX가 주는 WAV를 AAC로 변환하는 데 쓴다. brew install ffmpeg)

이미 캐시 파일이 있는 조합은 건너뛰므로, 중간에 실패하거나 멈춰도 다시 실행하면 이어서 진행된다.
단어/딕테이션 문장을 새로 추가한 뒤에도 이 스크립트를 다시 돌리면 새로 추가된 것만 생성된다.
"""

import hashlib
import json
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
# data.sql은 손으로 관리하는 시드, data-vocab.sql은 generate_vocab_sql.py가 만드는 자동 생성분.
# 두 파일에 있는 단어를 모두 캐시 대상으로 삼아야 한다.
DATA_SQL_FILES = [
    PROJECT_ROOT / "src/main/resources/data.sql",
    PROJECT_ROOT / "src/main/resources/data-vocab.sql",
]
CACHE_DIR = PROJECT_ROOT / "src/main/resources/tts-cache"
BACKEND_URL = "http://localhost:8080/api/tts/speak"

# 백엔드 TtsService 의 AUDIO_EXTENSION 과 반드시 같아야 번들 캐시가 맞아떨어진다.
AUDIO_EXT = ".m4a"

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


def split_value_tuples(segment: str) -> tuple[list[str], int]:
    """VALUES 뒤의 '(...), (...), (...);' 구간을 튜플 목록과 소비한 길이로 반환한다.

    문자열 리터럴 안의 괄호/세미콜론에 속지 않도록 따옴표 상태를 추적한다.
    """
    tuples: list[str] = []
    depth = 0
    in_string = False
    buf: list[str] = []
    i, n = 0, len(segment)
    while i < n:
        ch = segment[i]
        if in_string:
            if ch == "'":
                if i + 1 < n and segment[i + 1] == "'":  # '' 는 이스케이프된 따옴표
                    buf.append("''")
                    i += 2
                    continue
                in_string = False
            buf.append(ch)
        elif ch == "'":
            in_string = True
            buf.append(ch)
        elif ch == "(":
            depth += 1
            if depth == 1:
                buf = []          # 튜플 시작 - 여는 괄호는 담지 않는다
            else:
                buf.append(ch)
        elif ch == ")":
            depth -= 1
            if depth == 0:
                tuples.append("".join(buf))
            else:
                buf.append(ch)
        elif ch == ";" and depth == 0:
            i += 1                # 한 INSERT 문의 끝
            break
        elif depth > 0:
            buf.append(ch)
        i += 1
    return tuples, i


def extract_texts(table: str, value_index: int) -> list[str]:
    """시드 SQL에서 특정 테이블의 INSERT 문을 찾아, 지정한 인덱스의 값만 뽑는다.

    data.sql은 한 줄에 한 행이지만, 자동 생성되는 data-vocab.sql은 기동 속도를 위해
    여러 행을 한 INSERT로 묶어두었다. 두 형태 모두 처리해야 한다.
    """
    marker = f"INSERT IGNORE INTO {table} ("
    values_marker = ") VALUES"
    texts: list[str] = []

    for path in DATA_SQL_FILES:
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        pos = 0
        while True:
            start = text.find(marker, pos)
            if start == -1:
                break
            vstart = text.find(values_marker, start)
            if vstart == -1:
                break
            body_start = vstart + len(values_marker)
            tuple_strs, consumed = split_value_tuples(text[body_start:])
            for tuple_str in tuple_strs:
                values = parse_sql_values(tuple_str)
                if value_index < len(values):
                    texts.append(values[value_index])
            pos = body_start + consumed

    return texts


def cache_key(text: str, speaker_id: int, speed_scale: float) -> str:
    """백엔드 TtsService.cacheKey()와 정확히 같은 방식으로 계산해야 같은 파일을 가리킨다.

    구분자는 공백이 아니라 NUL('\\0')이다. 일본어 문장에 공백/기호가 들어갈 수 있어서
    흔한 구분자를 쓰면 서로 다른 조합이 같은 키로 뭉개질 수 있기 때문이다.
    TtsService.cacheKey() 와 한 글자라도 달라지면 캐시가 통째로 어긋난다.
    """
    normalized_speed = f"{speed_scale:.2f}"
    raw = f"{text}\0{speaker_id}\0{normalized_speed}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def request_and_cache(text: str, speed_scale: float, voice_name: str, speaker_id: int) -> bool:
    """반환값: 실제로 새로 만들었으면 True, 이미 캐시 파일이 있어서 건너뛰었으면 False."""
    key = cache_key(text, speaker_id, speed_scale)
    target = CACHE_DIR / f"{key}{AUDIO_EXT}"
    if target.exists():
        return False

    body = json.dumps({"text": text, "speedScale": speed_scale, "voice": voice_name}).encode("utf-8")
    req = urllib.request.Request(
        BACKEND_URL, data=body, headers={"Content-Type": "application/json"}, method="POST"
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        raw = resp.read()  # 캐시에 없는 조합이므로 VOICEVOX 실시간 합성 결과(WAV)가 내려온다.

    # WAV 그대로 두면 용량이 7배 넘게 커진다. 커밋 전에 AAC로 변환해서 저장한다.
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp.write(raw)
        tmp_path = Path(tmp.name)
    try:
        subprocess.run(
            ["ffmpeg", "-v", "error", "-y", "-i", str(tmp_path),
             "-c:a", "aac", "-b:a", "48k", "-ac", "1", str(target)],
            check=True,
        )
    finally:
        tmp_path.unlink(missing_ok=True)
    return True


def main() -> None:
    if not any(p.exists() for p in DATA_SQL_FILES):
        print("시드 SQL을 찾을 수 없습니다: "
              + ", ".join(str(p) for p in DATA_SQL_FILES), file=sys.stderr)
        sys.exit(1)

    if shutil.which("ffmpeg") is None:
        print("ffmpeg이 필요합니다. macOS라면: brew install ffmpeg", file=sys.stderr)
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
