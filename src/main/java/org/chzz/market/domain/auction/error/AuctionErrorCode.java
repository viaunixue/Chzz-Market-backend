package org.chzz.market.domain.auction.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."),
    INVALID_AUCTION_STATE(HttpStatus.BAD_REQUEST, "경매 상태가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getCode() {
        return "";
    }
}
