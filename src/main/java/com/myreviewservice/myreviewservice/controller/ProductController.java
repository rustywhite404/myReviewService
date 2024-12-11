package com.myreviewservice.myreviewservice.controller;


import com.myreviewservice.myreviewservice.dto.ReviewRequestDto;
import com.myreviewservice.myreviewservice.dto.ReviewResponseDto;
import com.myreviewservice.myreviewservice.service.ProductService;
import com.myreviewservice.myreviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController{

    private final ProductService productService;
    private final ReviewService reviewService;

    @PostMapping("/{productId}/reviews")
    public void createReview(@PathVariable Long productId, @RequestBody ReviewRequestDto requestDto){
        log.info("productId:"+productId+", RequestDto:"+requestDto);
        reviewService.addReview(productId, requestDto);
    }

}
