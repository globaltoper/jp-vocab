import { describe, expect, it } from "vitest";
import { render, fireEvent, screen } from "@testing-library/react";
import { useRomajiInput } from "./useRomajiInput";

// useRomajiInput은 <input ref={inputRef}>가 "나중에" 조건부로 마운트되는 상황에서도
// wanakana 리스너가 정상적으로 붙어야 한다 (실제로 이 부분이 콜백 ref로 바꾸기 전에는 버그였다:
// 처음 렌더링 시점에 input이 아직 없으면 리스너가 영영 안 붙었다).
// 이 테스트는 정확히 그 시나리오(지연 마운트)를 재현한다.
function DelayedMountHarness() {
  const { inputRef, hiragana } = useRomajiInput();
  return (
    <div>
      <span data-testid="hiragana-output">{hiragana}</span>
      {/* 실제 딕테이션 페이지처럼, 조건이 true가 된 후에야 input이 렌더링되는 상황을 흉내낸다 */}
      <input ref={inputRef} data-testid="romaji-input" />
    </div>
  );
}

// 실제 키 입력은 "지금까지 변환된 값" 위에 새 글자 하나를 추가하는 식으로 일어난다.
// (wanakana가 매 입력마다 input.value 자체를 변환된 값으로 바꿔놓기 때문에,
//  원본 로마자 문자열을 처음부터 다시 세팅하면 안 되고 매번 현재 값 기준으로 이어써야 한다)
function typeCharByChar(input: HTMLInputElement, text: string) {
  for (const char of text) {
    fireEvent.input(input, { target: { value: input.value + char } });
  }
}

describe("useRomajiInput", () => {
  it("로마자를 한 글자씩 입력하면 히라가나로 실시간 변환되어 hiragana 상태에 반영된다", () => {
    render(<DelayedMountHarness />);
    const input = screen.getByTestId("romaji-input") as HTMLInputElement;

    typeCharByChar(input, "arigatou");

    expect(screen.getByTestId("hiragana-output").textContent).toBe("ありがとう");
  });

  it("촉음(っ, 자음 두 번 입력)이 포함된 로마자도 올바르게 변환된다", () => {
    render(<DelayedMountHarness />);
    const input = screen.getByTestId("romaji-input") as HTMLInputElement;

    typeCharByChar(input, "kitte");

    expect(screen.getByTestId("hiragana-output").textContent).toBe("きって");
  });

  it("마침표(.)는 일본어 마침표(。)로 변환된다", () => {
    render(<DelayedMountHarness />);
    const input = screen.getByTestId("romaji-input") as HTMLInputElement;

    typeCharByChar(input, "sou desu.");

    expect(screen.getByTestId("hiragana-output").textContent).toContain("。");
  });

  it("ん 뒤에 모음이 와서 모호할 때는 apostrophe(')로 명시적으로 끊어줘야 한다", () => {
    // "koNnichiwa"를 그냥 "konnichiwa"로 치면 wanakana가 nn을 겹자음으로 묶어버려서
    // 「こんいちわ」로 잘못 변환된다. 실제 일본어 로마자 입력 관례대로 apostrophe를 써야
    // 「こんにちわ」로 정확히 변환된다 - 이건 버그가 아니라 로마자 입력의 알려진 특성이라
    // 딕테이션 페이지의 안내 문구에도 반영할 가치가 있는 케이스라 테스트로 문서화해둔다.
    render(<DelayedMountHarness />);
    const input = screen.getByTestId("romaji-input") as HTMLInputElement;

    typeCharByChar(input, "kon'nichiwa");

    expect(screen.getByTestId("hiragana-output").textContent).toBe("こんにちわ");
  });

  it("입력이 비어있으면 hiragana도 빈 문자열이다", () => {
    render(<DelayedMountHarness />);
    expect(screen.getByTestId("hiragana-output").textContent).toBe("");
  });

  it("reset()을 호출하면 입력값과 hiragana 상태가 모두 초기화된다", () => {
    function ResetHarness() {
      const { inputRef, hiragana, reset } = useRomajiInput();
      return (
        <div>
          <span data-testid="hiragana-output">{hiragana}</span>
          <input ref={inputRef} data-testid="romaji-input" />
          <button onClick={reset}>reset</button>
        </div>
      );
    }
    render(<ResetHarness />);
    const input = screen.getByTestId("romaji-input") as HTMLInputElement;
    typeCharByChar(input, "aiueo");
    expect(screen.getByTestId("hiragana-output").textContent).toBe("あいうえお");

    fireEvent.click(screen.getByText("reset"));

    expect(screen.getByTestId("hiragana-output").textContent).toBe("");
    expect(input.value).toBe("");
  });
});
