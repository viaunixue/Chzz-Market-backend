package org.chzz.market.domain.auction.controller;

import com.amazonaws.services.s3.AmazonS3;
import org.chzz.market.MarketApplication;
import org.chzz.market.domain.auction.dto.request.AuctionCreateRequest;
import org.chzz.market.domain.auction.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {MarketApplication.class, AuctionControllerTest.TestConfig.class})
@ActiveProfiles("test")
class AuctionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("경매 등록 모든 필드 정상 입력 시 성공 응답")
    void testCreateAuction() throws Exception {

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes()
        );

        MockMultipartFile image3 = new MockMultipartFile(
                "images",
                "test3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 3".getBytes()
        );

        when(auctionService.createAuction(any(AuctionCreateRequest.class)))
                .thenReturn(1L);

        // When & Then
        mockMvc.perform(multipart("/api/v1/auctions")
                .file(image1)
                .file(image2)
                .file(image3)
                .param("userId", "1")
                .param("title", "Test Auction")
                .param("description", "Test Description")
                .param("category", "ELECTRONICS")
                .param("minPrice", "1000")
                .param("preOrder", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/auctions/1"))  // Location 헤더 검증 추가
                .andExpect(content().string(""));

        verify(auctionService, times(1)).createAuction(any(AuctionCreateRequest.class));
    }

    @Test
    @DisplayName("필수 필드 누락 시 Bad Request 응답")
    void testCreateAuctionMissingRequiredField() throws Exception {
        mockMvc.perform(multipart("/api/v1/auctions")
                        .param("userId", "1")
                        // title 누락
                        .param("description", "Test Description")
                        .param("category", "ELECTRONICS")
                        .param("minPrice", "1000")
                        .param("preOrder", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 가격 입력 시 Bad Request 응답")
    void testCreateAuctionInvalidPrice() throws Exception {
        mockMvc.perform(multipart("/api/v1/auctions")
                        .param("userId", "1")
                        .param("title", "Test Auction")
                        .param("description", "Test Description")
                        .param("category", "ELECTRONICS")
                        .param("minPrice", "invalid")
                        .param("preOrder", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Configuration
    static class TestConfig {

        @Bean
        @Primary
        @Profile("test")
        public AmazonS3 amazonS3() {
            return mock(AmazonS3.class);
        }
    }
}