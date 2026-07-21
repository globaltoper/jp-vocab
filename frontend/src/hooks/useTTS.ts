import { useCallback, useState } from "react";
import { API_BASE_URL } from "../api/client";

// 여러 컴포넌트에서 useTTS()를 각자 호출해도, 재생 중인 오디오는 앱 전체에서 하나만 있어야 한다.
// (브라우저의 window.speechSynthesis도 원래 전역 싱글턴이라 이 동작과 자연스럽게 맞다.)
let currentAudio: HTMLAudioElement | null = null;

// 같은 문장을 같은 속도로 다시 들을 때(딕테이션에서 "다시 듣기"를 누르는 경우가 많다) 매번
// VOICEVOX에 새로 합성 요청을 보내지 않도록 오디오를 캐싱한다. 탭을 새로고침하면 비워지는
// 메모리 캐시라 용량 걱정 없이 써도 된다.
const audioCache = new Map<string, Blob>();

function cacheKey(text: string, rate: number): string {
  return `${rate}::${text}`;
}

async function fetchTtsBlob(text: string, rate: number): Promise<Blob> {
  const key = cacheKey(text, rate);
  const cached = audioCache.get(key);
  if (cached) {
    return cached;
  }

  const response = await fetch(`${API_BASE_URL}/tts/speak`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text, speedScale: rate }),
  });

  if (!response.ok) {
    throw new Error("voicevox tts unavailable");
  }

  const blob = await response.blob();
  audioCache.set(key, blob);
  return blob;
}

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
        const blob = await fetchTtsBlob(text, rate);
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
        setIsSpeaking(false);
        speakWithBrowserFallback(text, rate);
      }
    },
    [speakWithBrowserFallback],
  );

  // 화면에 소리를 내지 않고 미리 합성해서 캐시에 담아두기만 한다.
  // 딕테이션 페이지가 새 문장을 불러오는 즉시 이걸 불러두면, 사용자가 "듣기"를 누르는
  // 시점에는 이미 캐시에 있어서 거의 즉시 재생된다.
  const preload = useCallback((text: string, rate = 1) => {
    fetchTtsBlob(text, rate).catch(() => {
      // 프리로드 실패는 조용히 무시한다 - 실제로 재생을 시도할 때 다시 시도되고,
      // 그때도 실패하면 브라우저 폴백으로 넘어간다.
    });
  }, []);

  return { speak, preload, isSpeaking };
}
