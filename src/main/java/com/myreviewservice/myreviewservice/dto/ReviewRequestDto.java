package com.myreviewservice.myreviewservice.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @Min(value = 1, message = "Score는 최소 1이어야 합니다.")
    @Max(value = 5, message = "Score는 최대 5이어야 합니다.")
    private int score;

    @NotNull(message = "Content는 필수 값입니다.")
    private String content;
    private String imageUrl;
}
