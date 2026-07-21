import { useCallback, useEffect, useRef, useState } from "react";
import * as wanakana from "wanakana";

/**
 * 로마자를 입력하면 실시간으로 히라가나로 변환해주는 훅.
 *
 * 왜 직접 만들지 않고 wanakana를 쓰는가:
 * - "n" 하나만 입력했을 때 ん으로 끝날지(예: "kan" → かん) な행으로 이어질지(예: "kana" → かな)는
 *   다음 글자가 와야 확정된다. wanakana는 이런 경우를 IME처럼 버퍼링해서 처리한다(IMEMode).
 * - っ(촉음, 자음 두 번 - "kitte" → きって), ゃゅょ(요음 - "kya" → きゃ) 같은 예외도
 *   전부 검증된 라이브러리가 처리하는 쪽이, 직접 정규식으로 짜는 것보다 훨씬 안정적이다.
 *
 * 사용법: input 엘리먼트에 ref={inputRef}를 걸어주면, 그 input에 로마자를 치는 즉시
 * 화면의 값 자체가 히라가나로 바뀌고, hiragana 상태값도 같이 갱신된다.
 */
export function useRomajiInput() {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [hiragana, setHiragana] = useState("");

  useEffect(() => {
    const el = inputRef.current;
    if (!el) return;

    // 1) wanakana가 이 input의 'input' 이벤트를 가로채서 로마자 -> 히라가나 변환을 먼저 수행한다.
    wanakana.bind(el, { IMEMode: true });

    // 2) 우리 리스너는 그 다음에 등록되므로, el.value를 읽을 때는 이미 변환이 끝난 값이다.
    //    (같은 엘리먼트의 같은 이벤트 타입에 붙은 네이티브 리스너는 등록 순서대로 실행된다)
    const handleInput = () => setHiragana(el.value);
    el.addEventListener("input", handleInput);

    return () => {
      wanakana.unbind(el);
      el.removeEventListener("input", handleInput);
    };
  }, []);

  const reset = useCallback(() => {
    if (inputRef.current) {
      inputRef.current.value = "";
    }
    setHiragana("");
  }, []);

  return { inputRef, hiragana, reset };
}
