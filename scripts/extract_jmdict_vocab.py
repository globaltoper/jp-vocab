#!/usr/bin/env python3
"""
JMdict에서 학습용 단어 후보를 뽑아 CSV로 저장한다. (한국어 번역 전 원본 데이터)

데이터 출처와 라이선스:
  JMdict / KANJIDIC2 - EDRDG, CC BY-SA 4.0. 자세한 조건은 저장소 루트의 LICENSES.md 참고.
  이 스크립트의 출력물은 JMdict의 파생 데이터이므로 동일하게 CC BY-SA 4.0으로 배포해야 한다.

단어 선정 기준 - 왜 ichi1 인가:
  JMdict의 빈도 태그 nf01~nf48은 '신문' 말뭉치 기준이라 학습용으로는 맞지 않는다.
  실제로 「食べる」는 nf25, 「飲む」는 nf35이고 「行く」는 아예 태그가 없다.
  신문에는 政治/経済 같은 단어가 자주 나오지만 食べる는 잘 안 나오기 때문이다.

  반면 ichi1 태그는 『一万語語彙分類集』에 실린 단어라는 뜻으로, 말 그대로 일상 상용어
  약 1만 개를 정리한 목록이다. 학습용 어휘 선정에는 이쪽이 맞다.

레벨 추정:
  JLPT 공식 어휘 목록은 2010년 이후 공개되지 않는다. 별도의 JLPT 목록 파일을 --jlpt 로
  넘기면 그 값을 그대로 쓰고, 없으면 KANJIDIC2의 한자 난이도(구 JLPT 등급/학년 배당)를
  이용해 추정한다. 추정한 항목은 level_estimated=1 로 표시된다.

사용법:
  pip install jamdict jamdict-data
  python3 scripts/extract_jmdict_vocab.py --out build/vocab_raw.csv

  # JLPT 목록(선택): "표제어<TAB>레벨" 형식의 TSV
  python3 scripts/extract_jmdict_vocab.py --jlpt data/jlpt_levels.tsv --out build/vocab_raw.csv
"""

import argparse
import csv
import os
import sqlite3
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent

# KANJIDIC2의 jlpt 필드는 2009년까지의 구 4단계 시험 기준(4=가장 쉬움, 1=가장 어려움)이다.
# 신 5단계(N5~N1)와 정확히 대응하지 않으므로 어디까지나 근사치다.
OLD_JLPT_TO_LEVEL = {"4": "N5", "3": "N4", "2": "N3", "1": "N2"}

# 구 JLPT 등급이 없는 한자는 학년 배당(grade)으로 대신 가늠한다.
# 1~6=초등 배당, 8=중고등 상용한자, 9/10=인명용.
GRADE_TO_LEVEL = {"1": "N5", "2": "N5", "3": "N4", "4": "N3", "5": "N3", "6": "N2", "8": "N1"}

LEVEL_ORDER = ["N5", "N4", "N3", "N2", "N1"]

# 학습용으로 부적절한 소수 항목만 최소한으로 거른다.
# 米国/韓国/天国처럼 실제로 배워야 할 단어까지 날리면 안 된다.
EXCLUDE_GLOSS_PATTERNS = (
    "former province",
    "republic of guatemala",
    "democratic people's republic",
)

POS_LABELS = [
    ("adjective (keiyoushi)", "형용사"),
    ("adjectival nouns", "형용동사"),
    ("na-adjective", "형용동사"),
    ("Ichidan verb", "동사"),
    ("Godan verb", "동사"),
    # 会議/勉強처럼 する가 붙는 명사는 JMdict가 'suru verb'도 같이 달아두는데,
    # 단어 자체는 명사이므로 noun 판정을 suru verb보다 먼저 본다.
    ("noun", "명사"),
    ("suru verb", "동사"),
    ("verb", "동사"),
    ("adverb", "부사"),
    ("pronoun", "대명사"),
    ("numeric", "수사"),
    ("counter", "조수사"),
    ("expression", "표현"),
    ("conjunction", "접속사"),
    ("particle", "조사"),
    ("prefix", "접두사"),
    ("suffix", "접미사"),
    ("interjection", "감탄사"),
]


def jamdict_db_path() -> Path:
    try:
        import jamdict_data
    except ImportError:
        print("jamdict-data가 필요합니다:  pip install jamdict jamdict-data", file=sys.stderr)
        sys.exit(1)
    return Path(os.path.dirname(jamdict_data.__file__)) / "jamdict.db"


def short_pos(pos_texts: list[str]) -> str:
    joined = " ".join(pos_texts).lower()
    for needle, label in POS_LABELS:
        if needle.lower() in joined:
            return label
    return "기타"


def load_jlpt_map(path: Path) -> dict[str, str]:
    mapping: dict[str, str] = {}
    with path.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            parts = line.split("\t")
            if len(parts) < 2:
                continue
            expr, level = parts[0].strip(), parts[1].strip().upper()
            if level in OLD_JLPT_TO_LEVEL.values() or level == "N1":
                mapping[expr] = level
    return mapping


def build_kanji_difficulty(conn: sqlite3.Connection) -> dict[str, str]:
    """한자 한 글자 -> 추정 레벨."""
    table: dict[str, str] = {}
    for row in conn.execute("SELECT literal, grade, jlpt FROM character"):
        literal, grade, jlpt = row["literal"], row["grade"], row["jlpt"]
        level = OLD_JLPT_TO_LEVEL.get(jlpt) if jlpt else None
        if level is None and grade:
            level = GRADE_TO_LEVEL.get(grade)
        table[literal] = level or "N1"
    return table


def estimate_level(expression: str, kanji_difficulty: dict[str, str]) -> str:
    """단어에 쓰인 한자 중 가장 어려운 것을 그 단어의 레벨로 본다. 가나만 있으면 N5."""
    levels = [kanji_difficulty[ch] for ch in expression if ch in kanji_difficulty]
    if not levels:
        return "N5"
    return max(levels, key=LEVEL_ORDER.index)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--tags", default="ichi1",
                    help="선정에 사용할 JMdict 우선순위 태그 (쉼표 구분, 기본 ichi1)")
    ap.add_argument("--jlpt", type=Path, default=None,
                    help="JLPT 레벨 TSV (선택). 없으면 한자 난이도로 추정한다")
    ap.add_argument("--out", type=Path, default=PROJECT_ROOT / "build/vocab_raw.csv")
    ap.add_argument("--max-glosses", type=int, default=3, help="단어당 영어 뜻 최대 개수")
    args = ap.parse_args()

    conn = sqlite3.connect(jamdict_db_path())
    conn.row_factory = sqlite3.Row

    jlpt_map = load_jlpt_map(args.jlpt) if args.jlpt else {}
    if args.jlpt:
        print(f"JLPT 목록 {len(jlpt_map):,}개 로드")

    kanji_difficulty = build_kanji_difficulty(conn)
    tags = [t.strip() for t in args.tags.split(",") if t.strip()]
    ph = ",".join("?" * len(tags))

    # 표제어에 한자가 있으면 한자 표기를, 없으면(ある, これ 등) 가나 표기를 대표형으로 쓴다.
    entries = conn.execute(f"""
        SELECT idseq,
               MIN(CASE WHEN src = 'kanji' THEN text END) AS kanji_text,
               MIN(CASE WHEN src = 'kana'  THEN text END) AS kana_text
        FROM (
            SELECT k.idseq, k.text, 'kanji' AS src, k.ID AS rid
            FROM Kanji k JOIN KJP p ON p.kid = k.ID WHERE p.text IN ({ph})
            UNION ALL
            SELECT k.idseq, k.text, 'kana' AS src, k.ID AS rid
            FROM Kana k JOIN KNP p ON p.kid = k.ID WHERE p.text IN ({ph})
        )
        GROUP BY idseq
    """, tags + tags).fetchall()
    print(f"{'/'.join(tags)} 태그 항목: {len(entries):,}")

    reading_stmt = """
        SELECT text FROM Kana
        WHERE idseq = ? AND (nokanji IS NULL OR nokanji = 0)
        ORDER BY ID LIMIT 1
    """
    gloss_stmt = """
        SELECT sg.text FROM Sense s
        JOIN SenseGloss sg ON sg.sid = s.ID
        WHERE s.idseq = ? AND (sg.lang = 'eng' OR sg.lang IS NULL)
        ORDER BY s.ID, sg.rowid
    """
    # 품사는 첫 번째 뜻 기준. 모든 뜻을 합치면 「一つ」처럼 부차적 용법이 대표로 잡힌다.
    pos_stmt = "SELECT text FROM pos WHERE sid = (SELECT MIN(ID) FROM Sense WHERE idseq = ?)"
    # 신문 빈도 밴드는 레벨 판정엔 안 쓰고, '자주 쓰이는 순' 정렬에만 참고한다.
    band_stmt = """
        SELECT MIN(CAST(SUBSTR(p.text, 3) AS INTEGER))
        FROM Kanji k JOIN KJP p ON p.kid = k.ID
        WHERE k.idseq = ? AND p.text LIKE 'nf%'
    """

    rows = []
    seen: set[str] = set()
    skipped = {"reading": 0, "gloss": 0, "excluded": 0, "dup": 0}

    for e in entries:
        idseq = e["idseq"]
        expression = e["kanji_text"] or e["kana_text"]
        if not expression:
            continue

        r = conn.execute(reading_stmt, (idseq,)).fetchone()
        reading = r["text"] if r else (e["kana_text"] or "")
        if not reading:
            skipped["reading"] += 1
            continue

        glosses = [g["text"] for g in conn.execute(gloss_stmt, (idseq,))][: args.max_glosses]
        if not glosses:
            skipped["gloss"] += 1
            continue
        english = "; ".join(glosses)

        if any(p in english.lower() for p in EXCLUDE_GLOSS_PATTERNS):
            skipped["excluded"] += 1
            continue
        if expression in seen:
            skipped["dup"] += 1
            continue
        seen.add(expression)

        pos_texts = [p["text"] for p in conn.execute(pos_stmt, (idseq,))]
        band = conn.execute(band_stmt, (idseq,)).fetchone()[0]
        level = jlpt_map.get(expression)

        rows.append({
            "idseq": idseq,
            "expression": expression,
            "reading": reading,
            "english": english,
            "part_of_speech": short_pos(pos_texts),
            "level": level or estimate_level(expression, kanji_difficulty),
            "level_estimated": 0 if level else 1,
            "freq_band": band if band is not None else "",
        })

    # 자주 쓰이는 단어가 앞에 오도록 정렬(밴드 없는 항목은 뒤로).
    rows.sort(key=lambda r: (r["freq_band"] if r["freq_band"] != "" else 99, r["expression"]))

    args.out.parent.mkdir(parents=True, exist_ok=True)
    with args.out.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)

    print(f"\n추출 완료: {len(rows):,}개 -> {args.out}")
    print("  제외: " + ", ".join(f"{k} {v}" for k, v in skipped.items()))
    by_level: dict[str, int] = {}
    for r in rows:
        by_level[r["level"]] = by_level.get(r["level"], 0) + 1
    print("  레벨 분포: " + ", ".join(f"{lv} {by_level.get(lv, 0):,}" for lv in LEVEL_ORDER))
    if not jlpt_map:
        print("  (레벨은 한자 난이도 기반 추정치입니다. 실제 JLPT 목록은 --jlpt 옵션으로 지정)")


if __name__ == "__main__":
    main()
