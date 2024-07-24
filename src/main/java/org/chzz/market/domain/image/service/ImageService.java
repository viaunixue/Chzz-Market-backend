package org.chzz.market.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageUploader imageUploader;
    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;


    // 이미지 업로드 및 저장 로직 구현
    public String uploadImagesToCdn(MultipartFile image, Product product) {
        String fileName = generateFileName(image);
        String cdnPath = "";

        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, image.getInputStream(), new ObjectMetadata());
            amazonS3Client.putObject(putObjectRequest);

            // CDN 경로 생성 (전체 URL 아닌 경로만)
            cdnPath = "/" + fileName;

            // DB에 경로만 저장
            Image imageEntity = Image.builder()
                    .cdnPath(cdnPath)
                    .product(product)
                    .build();

            imageRepository.save(imageEntity);

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드를 실패했습니다.", e);
        }

        return cdnPath;
    }

    /**
     * 상품 이미지를 업로드하고 DB에 저장 (지금은 Test 용)
     */
    public List<String> saveProductImages(Product product, List<MultipartFile> imageUrls) {
        // Test 용 코드
        return imageUrls.stream().map(imageUrl -> {
            String cdnPath = imageUploader.uploadImage(imageUrl);
            Image imageEntity = Image.builder()
                    .cdnPath(cdnPath)
                    .product(product)
                    .build();
            imageRepository.save(imageEntity);
            return cdnPath;
        }).collect(Collectors.toList());

        // 추후 실제 코드
        // return imageUrls.stream()
        //         .map(imageUrl -> uploadImagesToCdn(imageUrl, product))
        //         .collect(Collectors.toList());
    }

    // 필요할 때 전체 URL 재구성
    public String getFullImageUrl(String cdnPath) {
        return "https://" + cloudfrontDomain + cdnPath;
    }

    // 파일 이름 생성
    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
    }
}

