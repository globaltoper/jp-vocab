import { useCallback, useRef, useState } from "react";
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
 *
 * 왜 콜백 ref인가 (일반 useRef + useEffect가 아니라):
 * - 이 훅을 쓰는 화면(딕테이션 등)은 문장을 비동기로 불러온 뒤에야 <input>이 렌더링된다.
 *   즉 최초 렌더 시점에는 input이 DOM에 아직 없다. useEffect(() => {...}, [])는 "마운트 시 딱 한 번"만
 *   실행되는데, 그 순간 input이 없으면 리스너가 영영 안 붙는다(나중에 input이 생겨도 재실행 안 됨).
 * - 콜백 ref는 React가 그 DOM 노드를 실제로 붙이거나 뗄 때마다 직접 호출해주므로,
 *   조건부 렌더링 여부와 상관없이 항상 정확한 타이밍에 붙는다.
 */
export function useRomajiInput() {
  const [hiragana, setHiragana] = useState("");
  const elementRef = useRef<HTMLInputElement | null>(null);
  const inputListenerRef = useRef<(() => void) | null>(null);

  const detach = useCallback(() => {
    const el = elementRef.current;
    if (el) {
      wanakana.unbind(el);
      if (inputListenerRef.current) {
        el.removeEventListener("input", inputListenerRef.current);
      }
    }
    elementRef.current = null;
    inputListenerRef.current = null;
  }, []);

  const inputRef = useCallback(
    (el: HTMLInputElement | null) => {
      detach();
      elementRef.current = el;
      if (el) {
        wanakana.bind(el, { IMEMode: true });
        const handleInput = () => setHiragana(el.value);
        inputListenerRef.current = handleInput;
        el.addEventListener("input", handleInput);
      }
    },
    [detach],
  );

  const reset = useCallback(() => {
    if (elementRef.current) {
      elementRef.current.value = "";
    }
    setHiragana("");
  }, []);

  return { inputRef, hiragana, reset };
}
