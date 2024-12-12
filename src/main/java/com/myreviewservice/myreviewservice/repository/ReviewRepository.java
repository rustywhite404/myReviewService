package com.myreviewservice.myreviewservice.repository;

import com.myreviewservice.myreviewservice.dto.ReviewStatsDto;
import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    String FIND_DTO = " new com.myreviewservice.myreviewservice.dto.";
    boolean existsByProductAndUserId(Product product, Long userid);

    @Query("SELECT"+FIND_DTO+"ReviewStatsDto(COUNT(r), AVG(r.score)) " +
            "FROM Review r WHERE r.product = :product")
    ReviewStatsDto findReviewStatsByProduct(@Param("product") Product product);

    Page<Review> findByProductId(Long productId, Pageable pageable);
}
