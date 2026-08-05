#!/usr/bin/env python3
"""
추출한 JMdict 단어 + 한국어 번역을 합쳐서 시드 SQL(data-vocab.sql)을 만든다.

입력:
  build/vocab_raw.csv        - extract_jmdict_vocab.py 의 출력 (표제어/읽기/영어뜻/품사/레벨)
  data/translations_ko.tsv   - 표제어 -> 한국어 뜻

출력:
  src/main/resources/data-vocab.sql

설계 메모:
  * words.expression 에는 유니크 제약이 없어서 INSERT IGNORE로는 표제어 중복이 걸러지지 않는다.
    그래서 기존 data.sql에 이미 있는 표제어는 생성 단계에서 제외한다(그쪽은 예문이 연결돼 있다).
  * id를 명시적으로 박아서 INSERT IGNORE가 기본키 기준으로 멱등하게 동작하게 한다.
    기존 data.sql이 1~110을 쓰므로 여유를 두고 ID_START부터 시작한다.
  * 한 줄에 한 INSERT를 쓰면 2,500개가 넘는 문장이 매 기동마다 순차 실행되어 느리다.
    여러 행을 한 INSERT로 묶어서 문장 수를 수십 개로 줄인다.

사용법:
  python3 scripts/extract_jmdict_vocab.py --out build/vocab_raw.csv
  python3 scripts/generate_vocab_sql.py
"""

import argparse
import csv
import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
ID_START = 1001
ROWS_PER_STATEMENT = 200


def sql_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "''")


def load_existing_expressions(data_sql: Path) -> set[str]:
    """기존 data.sql이 이미 넣고 있는 표제어."""
    text = data_sql.read_text(encoding="utf-8")
    return set(re.findall(r"INSERT IGNORE INTO words \([^)]*\) VALUES \(\d+, '([^']*)'", text))


def load_translations(path: Path) -> dict[str, str]:
    mapping: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        parts = line.split("\t")
        if len(parts) >= 2 and parts[1].strip():
            mapping[parts[0].strip()] = parts[1].strip()
    return mapping


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--raw", type=Path, default=PROJECT_ROOT / "build/vocab_raw.csv")
    ap.add_argument("--translations", type=Path, default=PROJECT_ROOT / "data/translations_ko.tsv")
    ap.add_argument("--out", type=Path, default=PROJECT_ROOT / "src/main/resources/data-vocab.sql")
    ap.add_argument("--levels", default="N5,N4", help="포함할 레벨 (쉼표 구분)")
    args = ap.parse_args()

    levels = {lv.strip() for lv in args.levels.split(",") if lv.strip()}
    translations = load_translations(args.translations)
    existing = load_existing_expressions(PROJECT_ROOT / "src/main/resources/data.sql")

    rows = []
    seen: set[str] = set()
    skipped_no_translation = skipped_existing = skipped_dup = 0

    for r in csv.DictReader(args.raw.open(encoding="utf-8")):
        if r["level"] not in levels:
            continue
        expr = r["expression"]
        if expr in existing:
            skipped_existing += 1
            continue
        if expr in seen:
            skipped_dup += 1
            continue
        meaning = translations.get(expr)
        if not meaning:
            skipped_no_translation += 1
            continue
        seen.add(expr)
        rows.append((expr, r["reading"], meaning, r["level"], r["part_of_speech"]))

    lines = [
        "-- 자동 생성 파일입니다. 직접 수정하지 마세요.",
        "-- 생성: scripts/generate_vocab_sql.py",
        "--",
        "-- 데이터 출처: JMdict (EDRDG, CC BY-SA 4.0) - 저장소 루트의 LICENSES.md 참고.",
        "-- 이 파일은 JMdict의 파생 데이터이므로 동일하게 CC BY-SA 4.0으로 배포됩니다.",
        "-- 한국어 뜻은 JMdict의 영어 뜻을 옮긴 것이며, JLPT 레벨은 한자 난이도 기반 추정치입니다.",
        "",
    ]

    for chunk_start in range(0, len(rows), ROWS_PER_STATEMENT):
        chunk = rows[chunk_start:chunk_start + ROWS_PER_STATEMENT]
        values = []
        for offset, (expr, reading, meaning, level, pos) in enumerate(chunk):
            wid = ID_START + chunk_start + offset
            values.append(
                f"({wid}, '{sql_escape(expr)}', '{sql_escape(reading)}', "
                f"'{sql_escape(meaning)}', '{level}', '{sql_escape(pos)}', NOW())"
            )
        lines.append(
            "INSERT IGNORE INTO words (id, expression, furigana, meaning, level, part_of_speech, created_at) VALUES"
        )
        lines.append(",\n".join(values) + ";")
        lines.append("")

    args.out.write_text("\n".join(lines), encoding="utf-8")

    statements = (len(rows) + ROWS_PER_STATEMENT - 1) // ROWS_PER_STATEMENT
    print(f"생성 완료: {len(rows):,}개 단어 -> {args.out}")
    print(f"  INSERT 문 수: {statements}개 (행 {ROWS_PER_STATEMENT}개씩 묶음)")
    print(f"  id 범위: {ID_START} ~ {ID_START + len(rows) - 1}")
    print(f"  제외: 기존 data.sql과 중복 {skipped_existing}, "
          f"번역 없음 {skipped_no_translation}, 목록 내 중복 {skipped_dup}")
    by_level: dict[str, int] = {}
    for _, _, _, lv, _ in rows:
        by_level[lv] = by_level.get(lv, 0) + 1
    print("  레벨 분포: " + ", ".join(f"{k} {v:,}" for k, v in sorted(by_level.items())))


if __name__ == "__main__":
    main()
