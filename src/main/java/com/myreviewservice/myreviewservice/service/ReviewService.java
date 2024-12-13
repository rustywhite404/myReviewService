package com.myreviewservice.myreviewservice.service;

import com.myreviewservice.myreviewservice.dto.ReviewRequestDto;
import com.myreviewservice.myreviewservice.dto.ReviewResponseDto;
import com.myreviewservice.myreviewservice.dto.ReviewStatsDto;
import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.entity.Review;
import com.myreviewservice.myreviewservice.exception.MyReviewServiceErrorCode;
import com.myreviewservice.myreviewservice.exception.MyReviewServiceException;
import com.myreviewservice.myreviewservice.repository.ProductRepository;
import com.myreviewservice.myreviewservice.repository.ReviewRepository;
import com.myreviewservice.myreviewservice.util.DummyS3Uploader;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.myreviewservice.myreviewservice.exception.MyReviewServiceErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final DummyS3Uploader dummyS3Uploader;
    private final RedissonClient redissonClient;

    private static final String REDIS_REVIEW_STATS_KEY = "product:reviewStats";

    @Transactional
    public void addReview(Long productId, ReviewRequestDto requestDto, MultipartFile image) {
        //상품이 존재하는지 확인(없으면 예외처리)
        Product product = productRepository.findById(productId).orElseThrow(() -> new MyReviewServiceException(NO_PRODUCT));

        //이 유저가 기존에 해당 상품에 리뷰를 남긴 적 없는지 확인(중복 리뷰 불가)
        boolean hasReview = reviewRepository.existsByProductAndUserId(product, requestDto.getUserId());
        if (hasReview) throw new MyReviewServiceException(DUPLICATED_REVIEW);

        // 이미지 파일 처리(더미 S3 구현체)
        String imageUrl = null;
        if (image != null && !image.isEmpty()) imageUrl = dummyS3Uploader.upload(image);

        //리뷰 저장
        Review review = Review.builder()
                .product(product)
                .userId(requestDto.getUserId())
                .score(requestDto.getScore())
                .content(requestDto.getContent())
                .imageUrl(imageUrl)
                .build();
        reviewRepository.save(review);

        // Redis에 리뷰 수 및 총합 점수 업데이트
        updateReviewStatsInRedis(productId, requestDto.getScore());

    }

    private void updateReviewStatsInRedis(Long productId, int score) {
        String key = REDIS_REVIEW_STATS_KEY;
        RLock lock = redissonClient.getLock("lock:product:review:" + productId);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // Redis에 리뷰 수 및 총합 점수 업데이트
                Map<String, Long> reviewStats = redissonClient.getMap(key);
                reviewStats.merge(productId + ":count", 1L, Long::sum); //리뷰 수 +1
                reviewStats.merge(productId + ":sum", (long)score, Long::sum); //점수 합계 업데이트

                // 동기화 상태 초기화
                reviewStats.put(productId + ":synced", 0L); // 동기화 상태 초기화

                log.info("Redis에 리뷰 데이터 업데이트: productId={}, count={}, sum={}", productId, reviewStats.get(productId + ":count"), reviewStats.get(productId + ":sum"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MyReviewServiceException(MyReviewServiceErrorCode.REDIS_ROCK_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    //동시성을 고려하여 리뷰 등록 및 평점 업데이트
    public void updateReviewCountAndAverageScoreWithLock(Long productId) {
        String lockKey = "product:lock:" + productId; //상품별 고유 락 키
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //락 최대 5초 대기, 락 획득 후 10초간 유지
            if(lock.tryLock(5,10, TimeUnit.SECONDS)){
                Product product = productRepository.findById(productId).orElseThrow(() -> new MyReviewServiceException(NO_PRODUCT));
                updateReviewCountAndAverageScore(product);
            }else {
                throw new MyReviewServiceException(MyReviewServiceErrorCode.RESOURCE_LOCK_FAILURE);
            }
        }   catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            throw new MyReviewServiceException(MyReviewServiceErrorCode.LOCK_ACQUISITION_ERROR);
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 락 해제
            }
        }
    }

    public ReviewResponseDto getReviews(Long productId, int cursor, int size) {
        //상품이 존재하는지 확인(없으면 예외처리)
        Product product = productRepository.findById(productId).orElseThrow(() -> new MyReviewServiceException(NO_PRODUCT));

        Pageable pageable = PageRequest.of(cursor, size, Sort.by("createdAt").descending());
        //해당 상품번호의 리뷰 전체 조회
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);

        if (page.isEmpty()) {
            return ReviewResponseDto.builder()
                    .totalCount(product.getReviewCount())
                    .score(product.getScore().floatValue())
                    .cursor(cursor)
                    .reviews(Collections.emptyList())
                    .errorCode(MyReviewServiceErrorCode.NO_REVIEWS)
                    .errorMessage(MyReviewServiceErrorCode.NO_REVIEWS.getMessage())
                    .build();
        }

        // 리뷰가 존재하면 변환
        List<ReviewResponseDto.ReviewDetailDto> reviewDetails = page.getContent().stream()
                .map(review -> new ReviewResponseDto.ReviewDetailDto(review))
                .collect(Collectors.toList());

        return ReviewResponseDto.builder()
                .totalCount(product.getReviewCount())
                .score(product.getScore().floatValue())
                .cursor(cursor)
                .reviews(reviewDetails)
                .build();
    }

    /**
     * 상품의 리뷰 개수와 평균 평점을 Optimistic Lock을 사용해 업데이트
     */
    @Transactional
    public void updateReviewCountAndAverageScore(Product product) {
        //단일책임원칙에 따라 ReviewCount와 AverageScore를 각각 메서드로 만들까 고민해보았는데
        //어차피 항상 같이 변경되는 기능이고, 데이터가 많이 쌓이는 테이블이라면 한 번 조회해서 결과를 이용하는 게 성능이 최적화 될 것 같아서 합침.

        //리뷰 개수와 평균 점수를 한번에 가져옴
        ReviewStatsDto reviewStats = reviewRepository.findReviewStatsByProduct(product);
        product.setReviewCount(reviewStats.getTotalCount());
        product.setScore(reviewStats.getAverageScore());
        productRepository.save(product);

    }

}
