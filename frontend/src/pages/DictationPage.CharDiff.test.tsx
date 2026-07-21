import { describe, expect, it } from "vitest";
import { render } from "@testing-library/react";
import { CharDiff } from "./DictationPage";

describe("CharDiff", () => {
  it("완전히 일치하면 모든 글자가 diff-match 클래스를 갖는다", () => {
    const { container } = render(<CharDiff typed="ありがとう" correct="ありがとう" />);
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(5);
    spans.forEach((span) => expect(span.className).toBe("diff-match"));
  });

  it("정답 텍스트를 표시하되, 위치별로 일치 여부에 따라 클래스가 갈린다", () => {
    const { container } = render(<CharDiff typed="ありがとう" correct="ありがとお" />);
    const spans = container.querySelectorAll("span");
    // 정답(correct)의 글자를 그대로 보여준다 - 마지막 글자만 다름(う vs お)
    expect(Array.from(spans).map((s) => s.textContent).join("")).toBe("ありがとお");
    expect(spans[4].className).toBe("diff-miss");
    expect(spans[0].className).toBe("diff-match");
  });

  it("입력이 정답보다 짧으면 부족한 부분은 diff-miss로 표시된다", () => {
    const { container } = render(<CharDiff typed="あり" correct="ありがとう" />);
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(5);
    expect(spans[0].className).toBe("diff-match");
    expect(spans[1].className).toBe("diff-match");
    expect(spans[2].className).toBe("diff-miss");
  });

  it("입력이 비어있으면 정답 전체가 diff-miss로 표시된다", () => {
    const { container } = render(<CharDiff typed="" correct="ねこ" />);
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(2);
    spans.forEach((span) => expect(span.className).toBe("diff-miss"));
  });
});
