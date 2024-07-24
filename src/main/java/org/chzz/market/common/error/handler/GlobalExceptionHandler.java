package org.chzz.market.common.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.error.exception.UserNotFoundException;
import org.chzz.market.common.error.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex){
        ErrorResponse errorResponse = ErrorResponse.from(ex.getErrorCode());
        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

}
