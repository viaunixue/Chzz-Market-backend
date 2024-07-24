package org.chzz.market.common.error.exception;

import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserNotFoundException() {
        this.errorCode = ErrorCode.USER_NOT_FOUND;
    }

    public UserNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.USER_NOT_FOUND;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
