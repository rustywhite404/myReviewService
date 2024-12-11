package com.myreviewservice.myreviewservice.repository;

import com.myreviewservice.myreviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
