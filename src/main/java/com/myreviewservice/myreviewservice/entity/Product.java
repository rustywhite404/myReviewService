package com.myreviewservice.myreviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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
@Table(name = "product")
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 20)
    private Long id;

    @Column(length = 20, nullable = false)
    @ColumnDefault("0")
    private BigInteger reviewCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Float score;

    //상품에 여러 리뷰가 연결될 수 있으므로 1:N
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

}
