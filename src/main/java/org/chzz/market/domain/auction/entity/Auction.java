package org.chzz.market.domain.auction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.exception.AuctionException;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = true)
    private User winner;

    @Column
    @ThousandMultiple
    private Integer minPrice;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Getter
    @AllArgsConstructor
    public enum AuctionStatus {
        PENDING("대기 중"),
        PROCEEDING("진행 중"),
        ENDED("종료"),
        CANCELLED("취소 됨");

        private final String description;
    }

    // 대기 중 -> 진행 중 상태 변경
    public void convertToProceeding() {
        if (this.status != AuctionStatus.PENDING) {
            throw new AuctionException(AuctionErrorCode.INVALID_AUCTION_STATE);
        }
        this.status = AuctionStatus.PROCEEDING;
    }
}
