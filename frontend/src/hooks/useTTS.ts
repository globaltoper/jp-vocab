import { useCallback, useState } from "react";
import { API_BASE_URL } from "../api/client";

// 여러 컴포넌트에서 useTTS()를 각자 호출해도, 재생 중인 오디오는 앱 전체에서 하나만 있어야 한다.
// (브라우저의 window.speechSynthesis도 원래 전역 싱글턴이라 이 동작과 자연스럽게 맞다.)
let currentAudio: HTMLAudioElement | null = null;

export function useTTS() {
  const [isSpeaking, setIsSpeaking] = useState(false);

  // VOICEVOX(고품질 TTS) 서버가 아예 꺼져있거나 응답이 없을 때를 위한 최후의 수단.
  // 로봇 발음이지만, 최소한 무음보다는 낫다.
  const speakWithBrowserFallback = useCallback((text: string, rate: number) => {
    if (!("speechSynthesis" in window)) {
      alert("이 브라우저는 음성 재생(TTS)을 지원하지 않습니다.");
      return;
    }

    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "ja-JP";
    utterance.rate = rate;

    const voices = window.speechSynthesis.getVoices();
    const jaVoice = voices.find((voice) => voice.lang.startsWith("ja"));
    if (jaVoice) {
      utterance.voice = jaVoice;
    }

    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);
    window.speechSynthesis.speak(utterance);
  }, []);

  // rate: 1이 기본 속도. 0.7 정도면 딕테이션 연습용으로 느리게 들린다.
  const speak = useCallback(
    async (text: string, rate = 1) => {
      if (currentAudio) {
        currentAudio.pause();
        currentAudio = null;
      }
      if ("speechSynthesis" in window) {
        window.speechSynthesis.cancel();
      }

      setIsSpeaking(true);
      try {
        const response = await fetch(`${API_BASE_URL}/tts/speak`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ text, speedScale: rate }),
        });

        if (!response.ok) {
          throw new Error("voicevox tts unavailable");
        }

        const blob = await response.blob();
        const objectUrl = URL.createObjectURL(blob);
        const audio = new Audio(objectUrl);
        currentAudio = audio;

        const cleanup = () => {
          setIsSpeaking(false);
          URL.revokeObjectURL(objectUrl);
        };
        audio.onended = cleanup;
        audio.onerror = cleanup;

        await audio.play();
      } catch {
        // 로컬 개발 중 VOICEVOX를 안 띄웠거나, 서버가 잠시 죽었거나 하는 흔한 상황.
        // 사용자에게 에러를 보여주는 대신 조용히 브라우저 기본 음성으로 전환한다.
        speakWithBrowserFallback(text, rate);
      }
    },
    [speakWithBrowserFallback],
  );

  return { speak, isSpeaking };
}
