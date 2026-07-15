package com.toper.jpvocab.domain.review;

/**
 * 라이트너 박스별 다음 복습까지의 간격(일).
 */
final class LeitnerIntervals {

    private static final int[] DAYS_BY_BOX = {0, 1, 3, 7, 14, 30}; // index = boxLevel(1~5)

    private LeitnerIntervals() {
    }

    static int daysFor(int boxLevel) {
        if (boxLevel < 1 || boxLevel >= DAYS_BY_BOX.length) {
            return DAYS_BY_BOX[DAYS_BY_BOX.length - 1];
        }
        return DAYS_BY_BOX[boxLevel];
    }
}
