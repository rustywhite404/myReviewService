package com.myreviewservice.myreviewservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Long productId; //상품 ID
    private String userid;
    private int score;
    private String content;
    private String imageUrl;
}
