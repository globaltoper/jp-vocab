package com.toper.jpvocab.domain.dictation;

/**
 * 딕테이션 정답 채점용 유틸. "관대한 매칭"을 위해 공백/구두점을 무시하고,
 * 레벤슈타인 거리(편집 거리) 기반으로 100점 만점 유사도를 계산한다.
 * (완전히 똑같아야 정답 X, 몇 글자 틀려도 부분 점수를 준다)
 */
final class TextSimilarity {

    private TextSimilarity() {
    }

    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("[\\s。、！？!?.,]", "")
                .trim();
    }

    static int similarityPercent(String typed, String correct) {
        String a = normalize(typed);
        String b = normalize(correct);

        if (b.isEmpty()) {
            return a.isEmpty() ? 100 : 0;
        }

        int distance = levenshteinDistance(a, b);
        double ratio = 1.0 - ((double) distance / Math.max(a.length(), b.length()));
        int percent = (int) Math.round(ratio * 100);
        return Math.max(0, Math.min(100, percent));
    }

    private static int levenshteinDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            System.arraycopy(curr, 0, prev, 0, curr.length);
        }

        return prev[b.length()];
    }
}
