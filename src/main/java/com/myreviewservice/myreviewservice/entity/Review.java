package com.myreviewservice.myreviewservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger reviewId;

    @Column(nullable = false)
    private BigInteger userId;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private String content;

    @Column(nullable = true)
    private String imageUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    //리뷰는 하나의 상품에 속한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonProperty("id")
    @JsonIgnore // 무한 참조 방지
    private Product product;

}
