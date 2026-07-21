import { describe, expect, it } from "vitest";
import { render } from "@testing-library/react";
import { TypingTarget } from "./TypingPracticePage";

describe("TypingTarget", () => {
  it("아직 아무것도 안 쳤으면 첫 글자만 커서(typing-cursor), 나머지는 typing-pending", () => {
    const { container } = render(<TypingTarget target="ねこ" typed="" />);
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(2);
    expect(spans[0].className).toBe("typing-cursor");
    expect(spans[1].className).toBe("typing-pending");
  });

  it("맞게 친 글자는 typing-correct로 표시된다", () => {
    const { container } = render(<TypingTarget target="ねこ" typed="ね" />);
    const spans = container.querySelectorAll("span");
    expect(spans[0].className).toBe("typing-correct");
    expect(spans[1].className).toBe("typing-cursor");
  });

  it("틀리게 친 글자는 typing-incorrect로 표시된다", () => {
    const { container } = render(<TypingTarget target="ねこ" typed="ぬ" />);
    const spans = container.querySelectorAll("span");
    expect(spans[0].className).toBe("typing-incorrect");
  });

  it("전부 맞게 치면 커서 없이 전부 typing-correct다", () => {
    const { container } = render(<TypingTarget target="ねこ" typed="ねこ" />);
    const spans = container.querySelectorAll("span");
    spans.forEach((span) => expect(span.className).toBe("typing-correct"));
  });

  it("target 글자 수만큼 span이 렌더링된다", () => {
    const { container } = render(<TypingTarget target="おはようございます" typed="おはよ" />);
    expect(container.querySelectorAll("span")).toHaveLength(9);
  });
});
