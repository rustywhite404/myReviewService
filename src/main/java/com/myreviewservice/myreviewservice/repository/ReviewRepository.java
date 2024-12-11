package com.myreviewservice.myreviewservice.repository;

import com.myreviewservice.myreviewservice.dto.ReviewStatsDto;
import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductAndUserId(Product product, Long userid);

    @Query("SELECT new com.myreviewservice.myreviewservice.dto.ReviewStatsDto(COUNT(r), AVG(r.score)) " +
            "FROM Review r WHERE r.product = :product")
    ReviewStatsDto findReviewStatsByProduct(@Param("product") Product product);
}
