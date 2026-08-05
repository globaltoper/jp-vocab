# 외부 데이터 · 음성 라이선스

이 프로젝트가 사용하는 외부 자원과 각각의 라이선스 조건을 정리한 문서입니다.
**여기 적힌 출처 표시를 지우거나 옮기면 라이선스 위반이 됩니다.** 화면 하단 `Footer` 컴포넌트가
표시 의무를 담당하고 있으니, 그 컴포넌트를 수정할 때는 이 문서를 먼저 확인하세요.

## 요약

| 자원 | 용도 | 라이선스 | 상업적 이용 | 표시 위치 |
|---|---|---|---|---|
| JMdict (EDRDG) | 단어 · 읽기 · 뜻 | CC BY-SA 4.0 | 가능 | Footer(전 화면), README, 이 문서 |
| JLPT Resources (Jonathan Waller) | JLPT 레벨 구분 | CC BY | 가능 | Footer(전 화면), README, 이 문서 |
| Tatoeba (CC0 파일 한정) | 예문 | CC0 1.0 | 가능 | 표시 의무 없음 (선의로 기재) |
| VOICEVOX | 발음 음성 합성 | 캐릭터별 이용약관 | 가능(크레딧 조건) | Footer(전 화면), README |

---

## JMdict — 단어 · 읽기 · 뜻

- 출처: <https://www.edrdg.org/wiki/index.php/JMdict-EDICT_Dictionary_Project>
- 라이선스: [Creative Commons Attribution-ShareAlike 4.0](https://creativecommons.org/licenses/by-sa/4.0/)
- 저작권: James William Breen 및 The Electronic Dictionary Research and Development Group

라이선스 원문에서 확인한 핵심 조건:

- **상업적 이용에 제한이 없습니다.** 원문: *"provided the conditions above are met, there is NO
  restriction placed on commercial use of the files."*
- **앱 코드를 오픈소스로 공개할 의무가 없습니다.** 원문: *"Software using these files does not have
  to be under any form of open-source licence."*
- **화면 표시 의무**: 단어를 화면에 표시하는 웹 서버는 각 화면에 출처를 표시해야 합니다. 화면
  하단 메시지 형태로 충분하다고 명시되어 있어, 모든 페이지에 렌더되는 `Footer`가 이를 담당합니다.
- **ShareAlike**: JMdict에서 파생된 **데이터 파일**은 같은 CC BY-SA 4.0으로 배포해야 합니다.
  이 조건은 데이터에만 적용되며 애플리케이션 코드에는 적용되지 않습니다
  (`src/main/resources/data/LICENSE` 참고).
- **갱신 의무**: 라이선스 4항은 데이터를 최신 버전으로 정기 갱신하는 절차를 요구합니다.
  README의 "데이터 갱신" 섹션과 갱신 스크립트가 이 절차에 해당합니다.

## JLPT 레벨 구분 — Jonathan Waller, JLPT Resources

- 출처: <http://www.tanos.co.uk/jlpt/>
- 라이선스: Creative Commons BY ([사용 조건 원문](http://www.tanos.co.uk/jlpt/sharing/))

저작자가 직접 명시한 조건: *"Everything on this site (that I'm not selling), is licenced under
Creative Commons 'BY'. Basically this means... use anything here however you like (commercial or
non-commercial), but credit my site. (A link would be nice.)"*

- **판매 중인 상품(Grammar Plus 음원 등)은 이 라이선스에 포함되지 않습니다.** 이 프로젝트는
  무료로 공개된 어휘 목록만 사용하며, 유료 자료는 일절 사용하지 않습니다.
- **정확도 주의**: JLPT 공식 어휘 목록은 2010년 이후 공개되지 않습니다. 이 목록은 추정치이며,
  제작자 본인도 N1 목록의 단어가 실제 N2 시험에 출제된 사례를 언급하고 있습니다. 앱에서도
  레벨을 참고용으로 안내합니다.

## Tatoeba — 예문

- 출처: <https://tatoeba.org/> / 다운로드: <https://tatoeba.org/en/downloads>
- 이 프로젝트는 **CC0 1.0으로 배포되는 파일만** 사용합니다.

CC0는 출처 표시 의무가 없지만, 선의로 기재합니다. 일반 배포본은 CC BY 2.0 FR이라 문장별 저자
표시가 필요한데, 이를 피하기 위해 CC0 전용 파일만 사용합니다.

- **Tatoeba의 음성 데이터는 사용하지 않습니다.** 오디오는 기여자마다 라이선스가 다르고,
  라이선스 필드가 비어 있는 경우 Tatoeba 외부에서 재사용이 금지되어 있습니다.

## VOICEVOX — 발음 음성

- 출처: <https://voicevox.hiroshiba.jp/>
- 사용 캐릭터: 四国めたん(여성 음성), 玄野武宏(남성 음성)

캐릭터 음성을 사용하는 경우 크레딧 표시가 필수입니다. 미표시 상업 이용은 캐릭터당 별도 유료
라이선스가 필요하므로, 이 프로젝트는 `Footer`에 항상 크레딧을 표시합니다.
다른 캐릭터를 추가하거나 교체하면 `Footer.tsx` 문구도 반드시 함께 갱신해야 합니다.

---

## 이 프로젝트가 하지 않는 것

라이선스 위반 위험을 원천적으로 피하기 위해 아래는 사용하지 않습니다.

- 웹 스크래핑으로 수집한 데이터
- Tatoeba의 음성 파일
- Waller 사이트의 유료 상품
- 교재 · 문제집 등에서 옮긴 어휘/예문

## 면책

이 문서는 각 배포처의 라이선스 원문을 확인해 정리한 것이며, 법률 자문이 아닙니다.
상업적 규모로 확대하는 등 상황이 달라지면 각 원문을 다시 확인하시기 바랍니다.
