package com.myreviewservice.myreviewservice.service;

import com.myreviewservice.myreviewservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
}
