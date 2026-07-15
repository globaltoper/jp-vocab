package com.toper.jpvocab.common.dto;

/**
 * 공통 에러 응답 포맷.
 * {
 *   "status": 404,
 *   "code": "WORD_NOT_FOUND",
 *   "message": "..."
 * }
 */
public record ErrorResponse(int status, String code, String message) {
}
