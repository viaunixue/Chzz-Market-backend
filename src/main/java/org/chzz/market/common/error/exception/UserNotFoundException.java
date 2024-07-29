package org.chzz.market.common.error.exception;

import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserNotFoundException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
