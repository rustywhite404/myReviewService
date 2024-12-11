package com.myreviewservice.myreviewservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReviewStatsDto {
    private Long totalCount; // 총 리뷰 수
    private Double averageScore; // 평균 점수

    // JPA에서 사용될 생성자
    public ReviewStatsDto(Long totalCount, Double averageScore) {
        this.totalCount = totalCount != null ? totalCount : 0L;
        this.averageScore = averageScore != null ? averageScore : 0.0;
    }
}