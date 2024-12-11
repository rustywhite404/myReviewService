package com.myreviewservice.myreviewservice.controller;


import com.myreviewservice.myreviewservice.dto.ReviewRequestDto;
import com.myreviewservice.myreviewservice.dto.ReviewResponseDto;
import com.myreviewservice.myreviewservice.service.ProductService;
import com.myreviewservice.myreviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController{

    private final ProductService productService;
    private final ReviewService reviewService;

    @PostMapping(value = "/{productId}/reviews", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void createReview(@PathVariable Long productId, @RequestPart("review") @Valid ReviewRequestDto requestDto,
                             @RequestPart(value = "image", required = false)MultipartFile image){
        log.info("productId: {}, RequestDto: {}, Image: {}", productId, requestDto, image != null ? image.getOriginalFilename() : "No Image");
        reviewService.addReview(productId, requestDto, image);
    }

}
