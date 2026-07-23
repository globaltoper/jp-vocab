// VOICEVOX 이용약관상, 이 목소리들을 쓰는 이상 어디선가는 "VOICEVOX를 이용했다"와
// "어떤 캐릭터를 썼는지"가 눈에 띄게 표시되어야 한다(앱의 경우 소개 화면 등에 기재).
// 이 앱은 발음 듣기 전반에 VOICEVOX의 四国めたん(여성 음성)과 玄野武宏(남성 음성)을 사용한다.
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
    </footer>
  );
}
