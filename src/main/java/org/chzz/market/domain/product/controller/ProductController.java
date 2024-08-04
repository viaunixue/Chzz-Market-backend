package org.chzz.market.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.request.RegisterProductRequest;
import org.chzz.market.domain.product.dto.response.RegisterProductResponse;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    // 경매 등록
    @PostMapping("/register")
    public ResponseEntity<RegisterProductResponse> registerAuctionProduct(@ModelAttribute @Valid RegisterProductRequest request) {
        RegisterProductResponse response = productService.registerAuctionProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 사전 등록
    @PostMapping("/pre-register")
    public ResponseEntity<RegisterProductResponse> preRegisterProduct(@ModelAttribute @Valid RegisterProductRequest request) {
        RegisterProductResponse response = productService.preRegisterProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 경매 상품으로 전환
    @PostMapping("/{productId}/convert-to-auction")
    public ResponseEntity<RegisterProductResponse> convertToAuction(@PathVariable Long productId) {
        RegisterProductResponse response = productService.convertToAuction(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
