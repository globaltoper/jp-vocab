import { useCallback, useEffect, useState } from "react";

// VOICEVOX 목소리 중 성별이 명확한 둘만 노출한다 (백엔드 Voice enum과 이름을 맞춰야 한다).
// FEMALE = 四国めたん(시코쿠 메탄), MALE = 玄野武宏(쿠로노 타케히로).
export type Voice = "FEMALE" | "MALE";

const VOICE_STORAGE_KEY = "jpvocab_voice";

// 같은 브라우저 탭 안의 여러 컴포넌트(Navbar의 선택 버튼, 각 페이지의 useTTS)가
// 전부 이 값을 구독하게 하려고 커스텀 이벤트를 쓴다. localStorage 자체는 "storage" 이벤트가
// 있지만 그건 다른 탭에서 바뀔 때만 발생하고 같은 탭에서는 안 터지기 때문에 직접 이벤트를 쏜다.
const VOICE_CHANGE_EVENT = "jpvocab:voice-changed";

function readStoredVoice(): Voice {
  return localStorage.getItem(VOICE_STORAGE_KEY) === "MALE" ? "MALE" : "FEMALE";
}

export function useVoicePreference() {
  const [voice, setVoiceState] = useState<Voice>(readStoredVoice);

  useEffect(() => {
    function handleChange() {
      setVoiceState(readStoredVoice());
    }
    window.addEventListener(VOICE_CHANGE_EVENT, handleChange);
    return () => window.removeEventListener(VOICE_CHANGE_EVENT, handleChange);
  }, []);

  const setVoice = useCallback((next: Voice) => {
    localStorage.setItem(VOICE_STORAGE_KEY, next);
    window.dispatchEvent(new Event(VOICE_CHANGE_EVENT));
  }, []);

  return { voice, setVoice };
}
