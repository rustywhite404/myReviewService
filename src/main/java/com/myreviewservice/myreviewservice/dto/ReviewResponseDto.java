package com.myreviewservice.myreviewservice.dto;

import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewResponseDto {
    private Long totalCount; //해당 상품에 작성된 총 리뷰 수
    private Float score; //평균점수
    private Long cursor; //커서 값
    private List<ReviewDetailDto> reviews; //리뷰 목록

    public static class ReviewDetailDto{
        private Long reviewId;
        private Long userId;
        private int score;
        private String content;
        private String imageUrl;
    }
}
