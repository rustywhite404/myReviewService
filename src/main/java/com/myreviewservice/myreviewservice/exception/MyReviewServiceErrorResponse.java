package com.myreviewservice.myreviewservice.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyReviewServiceErrorResponse {
    private MyReviewServiceErrorCode errorCode;
    private String errorMessage;
}
