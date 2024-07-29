package org.chzz.market.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED("I001", HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드를 실패했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
