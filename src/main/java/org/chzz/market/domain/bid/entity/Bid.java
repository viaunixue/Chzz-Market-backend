package org.chzz.market.domain.bid.entity;

import jakarta.persistence.*;
import lombok.*;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;
import org.hibernate.annotations.ColumnDefault;

/**
 * bid 생성시 해당 경매에 입찰 기록을 통해 entity를 가져와 dirty checking 가능하도록 구현 예정
 */
@Entity
@Getter
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="bid_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User bidder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id",nullable = false)
    private Auction auction;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    @ColumnDefault(value = "3")
    private int count;

    public void adjustBidAmount(Long amount) {
        if (this.count <= 0)
            //TODO 2024 07 18 14:17:18 : custom error
            throw new IllegalStateException();
        this.amount = amount;
        this.count--;
    }
}
