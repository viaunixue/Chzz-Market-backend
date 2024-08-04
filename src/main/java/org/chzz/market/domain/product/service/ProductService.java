package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;

import org.chzz.market.domain.image.error.ImageErrorCode;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.product.dto.request.RegisterProductRequest;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.response.RegisterProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.ProductStatus;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.error.exception.ProductErrorCode;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuctionService auctionService;
    private final ImageService imageService;

    /**
     * 경매 상품 등록
     */
    public RegisterProductResponse registerAuctionProduct(RegisterProductRequest request) {
        logger.info("사용자 {}의 경매 상품 등록을 시작합니다", request.getUserId());
        List<String> uploadedImageUrls = new ArrayList<>();
        try {
            // 이미지 업로드
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                uploadedImageUrls = imageService.uploadImages(request.getImages());
                logger.info("상품 경매 등록에 {} 개의 이미지 업로드", uploadedImageUrls.size());
            }

            // 트랜잭션 내부에서 상품 경매 등록
            RegisterProductResponse response = registerProductAndAuction(request, uploadedImageUrls, ProductStatus.IN_AUCTION);
            logger.info("경매 상품이 성공적으로 등록되었습니다. 상품 ID: {}", response.productId());

            return response;

        } catch (Exception e) {
            // 예외 발생 시 업로드 된 이미지 삭제
            imageService.deleteUploadImages(uploadedImageUrls);
            logger.error("사용자 {}의 경매 상품 등록에 실패했습니다. 오류: {}", request.getUserId(), e.getMessage(), e);
            throw new ProductException(ProductErrorCode.PRODUCT_REGISTER_FAILED);
        }
    }

    /**
     * 상품 사전 등록
     */
    public RegisterProductResponse preRegisterProduct(RegisterProductRequest request) {
        logger.info("사용자 {}의 상품 사전 등록을 시작합니다", request.getUserId());
        List<String> uploadedImageUrls = new ArrayList<>();
        try {
            // 이미지 업로드
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                uploadedImageUrls = imageService.uploadImages(request.getImages());
                logger.info("상품 사전 등록에 {} 개의 이미지 업로드", uploadedImageUrls.size());
            }

            // 트랜잭션 내부에서 상품 사전 등록
            RegisterProductResponse response = registerProductAndAuction(request, uploadedImageUrls, ProductStatus.PRE_REGISTERED);
            logger.info("상품이 성공적으로 사전 등록되었습니다. 상품 ID: {}", response.productId());

            return response;
        } catch (Exception e) {
            // 이미지 업로드 실패 시 업로드 된 이미지 삭제
            imageService.deleteUploadImages(uploadedImageUrls);
            logger.error("사용자 {}의 상품 사전 등록에 실패했습니다. 오류: {}", request.getUserId(), e.getMessage(), e);
            throw new ProductException(ProductErrorCode.PRODUCT_REGISTER_FAILED);
        }
    }

    /**
     * 상품 등록 및 경매 생성
     */
    @Transactional
    protected RegisterProductResponse registerProductAndAuction(RegisterProductRequest request, List<String> imageUrls, ProductStatus status){
        logger.debug("상품 및 경매 등록 프로세스를 시작합니다");
        // 유저 유효성 검사
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.error("ID가 {}인 사용자를 찾을 수 없습니다", request.getUserId());
                    return new UserException(UserErrorCode.USER_NOT_FOUND);
                });

        Product product = createProduct(request, user, status);
        product = productRepository.save(product);
        logger.info("ID가 {}인 상품이 저장되었습니다", product.getId());

        // 이미지 엔티티 저장
        if (!imageUrls.isEmpty()) {
            imageService.saveProductImageEntities(product, imageUrls);
            logger.info("상품 ID {}에 대해 {} 개의 이미지 엔티티를 저장했습니다", product.getId(), imageUrls.size());
        }

        // 상품 상태에 따라 분기 처리
        if (status == ProductStatus.IN_AUCTION) {
            auctionService.proceedingAuctionForProduct(product);
            logger.info("상품 ID {}에 대해 진행 중인 경매를 생성했습니다", product.getId());
        } else if (status == ProductStatus.PRE_REGISTERED) {
            auctionService.pendingAuctionForProduct(product);
            logger.info("상품 ID {}에 대해 대기 중인 경매를 생성했습니다", product.getId());
        }

        return new RegisterProductResponse(product.getId(), product.getStatus(), "상품이 등록되었습니다.");
    }

    /**
     * 사전 등록된 상품 -> 경매 상품으로 전환
     */
    @Transactional
    public RegisterProductResponse convertToAuction(Long productId) {
        logger.info("상품을 경매로 전환하기 시작합니다. 상품 ID: {}", productId);
        try {
            // 상품 유효성 검사
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("ID가 {}인 상품을 찾을 수 없습니다", productId);
                        return new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
                    });

            // 상품 상태 유효성 검사
            if (product.getStatus() != ProductStatus.PRE_REGISTERED) {
                logger.error("전환을 위한 상품 상태가 유효하지 않습니다. 상품 ID: {}, 현재 상태: {}", productId, product.getStatus());
                throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATE);
            }

            // 상품 상태 사전 등록 -> 경매 등록 전환 및 저장
            product.convertToAuction();
            productRepository.save(product);
            logger.info("상품 상태가 [[ IN_AUCTION ]] 으로 변경되었습니다. 상품 ID: {}", productId);

            // 경매 상품 상태 대기 -> 진행 상태로 전환
            auctionService.convertPendingToProceeding(product);
            logger.info("경매 상태가 [[ PROCEEDING ]] 으로 변경되었습니다. 상품 ID: {}", productId);

            RegisterProductResponse response = new RegisterProductResponse(product.getId(), product.getStatus(), "경매 상품으로 전환되었습니다.");
            logger.info("상품이 성공적으로 경매로 전환되었습니다. 상품 ID: {}", productId);

            return response;
        } catch (Exception e) {
            logger.error("상품을 경매로 전환하는 데 실패했습니다. 상품 ID: {}. 오류: {}", productId, e.getMessage(), e);
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATE);
        }
    }

    /**
     * 상품 이미지 저장
     */
    @Transactional
    public List<String> saveProductImages(Product product, List<MultipartFile> imageFiles) {
        try {
            logger.info("상품 ID {}에 대한 이미지 저장 프로세스를 시작합니다", product.getId());
            List<String> cdnPaths = imageService.uploadImages(imageFiles);
            imageService.saveProductImageEntities(product, cdnPaths);
            logger.info("상품 ID {}에 대한 이미지가 성공적으로 저장되었습니다. 이미지 수: {}", product.getId(), cdnPaths.size());
            return cdnPaths;
        } catch (Exception e) {
            logger.error("상품 ID {}에 대한 이미지 저장에 실패했습니다. 오류: {}", product.getId(), e.getMessage(), e);
            throw new ImageException(ImageErrorCode.IMAGE_SAVE_FAILED);
        }
    }

    /**
     * 등록 상품 저장하기
     */
    private Product createProduct(RegisterProductRequest request, User user, ProductStatus status) {
        logger.debug("사용자: {}, 상태: {}에 대한 상품 엔티티를 생성합니다", user.getId(), status);
        // 상품 생성
        return Product.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .minPrice(request.getMinPrice())
                .status(status)
                .build();
    }
}
