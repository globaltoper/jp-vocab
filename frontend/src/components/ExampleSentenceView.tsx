import { useNavigate } from "react-router-dom";
import type { ExampleSentenceData } from "../api/types";
import { useTTS } from "../hooks/useTTS";

interface Segment {
  text: string;
  wordId: number | null;
}

function buildSegments(sentence: string, linkedWords: ExampleSentenceData["linkedWords"]): Segment[] {
  const sorted = [...linkedWords].sort((a, b) => a.startIndex - b.startIndex);
  const segments: Segment[] = [];
  let cursor = 0;

  for (const link of sorted) {
    if (link.startIndex > cursor) {
      segments.push({ text: sentence.slice(cursor, link.startIndex), wordId: null });
    }
    segments.push({ text: sentence.slice(link.startIndex, link.endIndex), wordId: link.wordId });
    cursor = link.endIndex;
  }

  if (cursor < sentence.length) {
    segments.push({ text: sentence.slice(cursor), wordId: null });
  }

  return segments;
}

export function ExampleSentenceView({ example }: { example: ExampleSentenceData }) {
  const navigate = useNavigate();
  const { speak } = useTTS();
  const segments = buildSegments(example.sentenceJp, example.linkedWords);

  return (
    <div className="example-card">
      <div className="example-jp">
        {segments.map((segment, index) =>
          segment.wordId !== null ? (
            <button
              key={index}
              type="button"
              className="linked-word"
              onClick={() => navigate(`/words/${segment.wordId}`)}
            >
              {segment.text}
            </button>
          ) : (
            <span key={index}>{segment.text}</span>
          ),
        )}
        <button
          type="button"
          className="tts-button-small"
          onClick={() => speak(example.sentenceJp)}
          aria-label="예문 발음 듣기"
        >
          🔊
        </button>
      </div>
      <div className="example-reading">{example.sentenceReading}</div>
      <div className="example-meaning">{example.sentenceMeaning}</div>
    </div>
  );
}
