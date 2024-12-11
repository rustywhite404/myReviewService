package com.myreviewservice.myreviewservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 20)
    private BigInteger id;

    @Column(length = 20, nullable = false)
    private BigInteger reviewCount;

    @Column(nullable = false)
    private Float score;

    //상품에 여러 리뷰가 연결될 수 있으므로 1:N
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

}
