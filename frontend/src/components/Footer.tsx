// 외부 데이터/음성의 라이선스가 요구하는 출처 표시를 모으는 곳.
//
// - VOICEVOX: 이 목소리들을 쓰는 이상 "VOICEVOX를 이용했다"와 "어떤 캐릭터를 썼는지"가
//   눈에 띄게 표시되어야 한다(앱의 경우 소개 화면 등에 기재).
// - JMdict(EDRDG): CC BY-SA 4.0. "웹 서버가 사전 기능이나 단어를 화면에 표시하는 경우,
//   각 화면마다 출처를 표시해야 한다"는 조건이 있다. 이 Footer는 모든 페이지에 렌더되므로
//   이 조건을 충족한다. 여기서 링크를 지우면 라이선스 위반이 되니 주의.
// - JLPT 레벨(Jonathan Waller): CC BY. 사이트 크레딧 + 링크 요구.
//
// 자세한 내용은 저장소 루트의 LICENSES.md 참고.
export function Footer() {
  return (
    <footer className="app-footer">
      <p>
        발음(TTS)은{" "}
        <a href="https://voicevox.hiroshiba.jp/" target="_blank" rel="noreferrer">
          VOICEVOX
        </a>
        의 「四国めたん」(여성 음성) · 「玄野武宏」(남성 음성)으로 생성됩니다.
      </p>
      <p>
        단어 · 읽기 · 뜻 데이터는{" "}
        <a
          href="https://www.edrdg.org/wiki/index.php/JMdict-EDICT_Dictionary_Project"
          target="_blank"
          rel="noreferrer"
        >
          JMdict
        </a>
        (EDRDG,{" "}
        <a href="https://creativecommons.org/licenses/by-sa/4.0/" target="_blank" rel="noreferrer">
          CC BY-SA 4.0
        </a>
        )를 사용합니다.
      </p>
      <p>
        JLPT 레벨 구분은{" "}
        <a href="http://www.tanos.co.uk/jlpt/" target="_blank" rel="noreferrer">
          Jonathan Waller의 JLPT Resources
        </a>
        (CC BY)를 참고했습니다. JLPT 공식 어휘 목록은 공개되지 않으므로 레벨은 참고용 추정치입니다.
      </p>
    </footer>
  );
}
