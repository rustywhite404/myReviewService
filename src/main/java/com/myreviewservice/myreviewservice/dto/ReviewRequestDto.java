package com.myreviewservice.myreviewservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewRequestDto {
    private Long productId; //상품 ID
    private Long userId;
    private int score;
    private String content;
    private String imageUrl;
}
