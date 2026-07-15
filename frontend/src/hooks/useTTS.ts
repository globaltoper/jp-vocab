import { useCallback } from "react";

export function useTTS() {
  const speak = useCallback((text: string) => {
    if (!("speechSynthesis" in window)) {
      alert("이 브라우저는 음성 재생(TTS)을 지원하지 않습니다.");
      return;
    }

    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "ja-JP";

    const voices = window.speechSynthesis.getVoices();
    const jaVoice = voices.find((voice) => voice.lang.startsWith("ja"));
    if (jaVoice) {
      utterance.voice = jaVoice;
    }

    window.speechSynthesis.speak(utterance);
  }, []);

  return { speak };
}
