package com.myreviewservice.myreviewservice.dto;

import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.entity.Review;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewResponseDto {
    private Long totalCount; //해당 상품에 작성된 총 리뷰 수
    private float score; //평균점수
    private int cursor; //커서 값
    private List<ReviewDetailDto> reviews; //리뷰 목록

    public ReviewResponseDto(Product product, int cursor, List<ReviewDetailDto> reviews) {
        this.totalCount = product.getReviewCount();
        this.score = product.getScore().floatValue();
        this.cursor = cursor;
        this.reviews = reviews;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class ReviewDetailDto{
        private Long Id;
        private Long userId;
        private int score;
        private String content;
        private String imageUrl;
        private String createdAt;

        public ReviewDetailDto(Review review) {
            this.Id = review.getId();
            this.userId = review.getUserId();
            this.score = review.getScore();
            this.content = review.getContent();
            this.imageUrl = review.getImageUrl();
            this.createdAt = review.getCreatedAt()
                            .atZone(ZoneId.systemDefault()) //현재 시스템의 기본 시간대
                            .withZoneSameInstant(ZoneId.of("UTC")) //UTC 시간대로 변환
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        }
    }
}
