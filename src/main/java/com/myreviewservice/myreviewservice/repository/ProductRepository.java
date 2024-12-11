package com.myreviewservice.myreviewservice.repository;

import com.myreviewservice.myreviewservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
