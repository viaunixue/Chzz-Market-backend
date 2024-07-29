package org.chzz.market.domain.auction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.chzz.market.common.error.exception.UserNotFoundException;
import org.chzz.market.domain.auction.dto.request.AuctionCreateRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.user.repository.UserRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.auction.entity.SortType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
  
    public Page<AuctionResponse> getAuctionListByCategory(Category category, 
                                                          SortType sortType, 
                                                          Long userId,
                                                          Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, sortType, userId, pageable);
    }

    @Transactional
    public Long createAuction(AuctionCreateRequest dto) {

        // 사용자 데이터 조회
        User seller = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 상품 데이터 저장
        Product product = Product.builder()
                .name(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .user(seller)
                .build();
        product = productRepository.save(product);

        // 경매 데이터 저장
        Auction auction = Auction.builder()
                .product(product)
                .minPrice(dto.getMinPrice())
                .status(dto.isPreOrder() ? Auction.Status.PRE_ORDER : Auction.Status.PENDING)
                .build();
        auction = auctionRepository.save(auction);

        // 이미지 처리
        List<String> cdnPaths = imageService.saveProductImages(product, dto.getImages());

        // 이미지 URL Logging
        cdnPaths.forEach(path -> logger.info("Uploaded image path: {}", imageService.getFullImageUrl(path)));

        return auction.getId();
    }
}