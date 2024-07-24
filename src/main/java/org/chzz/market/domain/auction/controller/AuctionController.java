package org.chzz.market.domain.auction.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.AuctionCreateRequest;
import org.chzz.market.domain.auction.service.AuctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionController {
    private final AuctionService auctionService;

    // 클라이언트에서 직접 S3에 업로드하는 경우
    // @PostMapping
    // public ResponseEntity<Long> createAuction(@RequestBody @Valid AuctionCreateRequest dto) {
    //     Long auctionId  = auctionService.createAuction(dto);
    //     return ResponseEntity.ok().body(auctionId);
    // }


    /**
     * 서버에서 S3에 업로드하는 경우
     */
    @PostMapping
    public ResponseEntity<Long> createAuction(@ModelAttribute @Valid AuctionCreateRequest dto) {
        Long auctionId  = auctionService.createAuction(dto);
        return ResponseEntity.ok().body(auctionId);
    }
}
