package com.myreviewservice.myreviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long totalCount; //해당 상품에 작성된 총 리뷰 수
    private Float score; //평균점수
    private Long cursor; //커서 값
    private List<ReviewDetailDto> reviews; //리뷰 목록

    public static class ReviewDetailDto{
        private BigInteger reviewId;
        private BigInteger userId;
        private int score;
        private String content;
        private String imageUrl;
    }
}
