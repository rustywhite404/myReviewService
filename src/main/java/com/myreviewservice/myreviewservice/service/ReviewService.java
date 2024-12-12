package com.myreviewservice.myreviewservice.service;

import com.myreviewservice.myreviewservice.dto.ReviewRequestDto;
import com.myreviewservice.myreviewservice.dto.ReviewResponseDto;
import com.myreviewservice.myreviewservice.dto.ReviewStatsDto;
import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.entity.Review;
import com.myreviewservice.myreviewservice.exception.MyReviewServiceException;
import com.myreviewservice.myreviewservice.repository.ProductRepository;
import com.myreviewservice.myreviewservice.repository.ReviewRepository;
import com.myreviewservice.myreviewservice.util.DummyS3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.myreviewservice.myreviewservice.exception.MyReviewServiceErrorCode.*;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final DummyS3Uploader dummyS3Uploader;

    @Transactional
    public void addReview(Long productId, ReviewRequestDto requestDto, MultipartFile image) {
        //상품이 존재하는지 확인(없으면 예외처리)
        Product product = productRepository.findById(productId).orElseThrow(()->new MyReviewServiceException(NO_PRODUCT));

        //이 유저가 기존에 해당 상품에 리뷰를 남긴 적 없는지 확인(중복 리뷰 불가)
        boolean hasReview = reviewRepository.existsByProductAndUserId(product, requestDto.getUserId());
        if(hasReview) throw new MyReviewServiceException(DUPLICATED_REVIEW);

        // 이미지 파일 처리(더미 S3 구현체)
        String imageUrl = null;
        if(image!=null && !image.isEmpty()) imageUrl = dummyS3Uploader.upload(image);

        //리뷰 저장
        Review review = Review.builder()
                .product(product)
                .userId(requestDto.getUserId())
                .score(requestDto.getScore())
                .content(requestDto.getContent())
                .imageUrl(imageUrl)
                .build();
        reviewRepository.save(review);

        //리뷰를 받은 상품의 리뷰 수 증가 + 평점 재계산
        updateReviewCountAndAverageScore(product);

    }

    private void updateReviewCountAndAverageScore(Product product){
        //단일책임원칙에 따라 ReviewCount와 AverageScore를 각각 메서드로 만들까 고민해보았는데
        //어차피 항상 같이 변경되는 기능이고, 데이터가 많이 쌓이는 테이블이라면 한 번 조회해서 결과를 이용하는 게 성능이 최적화 될 것 같아서 합침.

        //리뷰 개수와 평균 점수를 한번에 가져옴
        ReviewStatsDto reviewStats = reviewRepository.findReviewStatsByProduct(product);

        product.setReviewCount(reviewStats.getTotalCount());
        product.setScore(reviewStats.getAverageScore());
        productRepository.save(product);

    }

    public List<ReviewResponseDto> getReviews(Long productId, int cursor, int size) {
        //상품이 존재하는지 확인(없으면 예외처리)
        Product product = productRepository.findById(productId).orElseThrow(()->new MyReviewServiceException(NO_PRODUCT));

        Pageable pageable = PageRequest.of(cursor, size, Sort.by("createdAt").descending());
        //해당 상품번호의 리뷰 전체 조회
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);
        if(page.isEmpty()){
            throw new MyReviewServiceException(NO_REVIEWS);
        }

        // Page<Review> -> List<ReviewResponseDto>
        return page.getContent().stream()
                .map(review -> new ReviewResponseDto(
                        product,cursor,List.of(new ReviewResponseDto.ReviewDetailDto(review))
                )).collect(Collectors.toList());
    }
}
