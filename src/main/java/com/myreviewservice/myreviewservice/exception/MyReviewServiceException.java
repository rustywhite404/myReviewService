package com.myreviewservice.myreviewservice.exception;

import lombok.Getter;

@Getter
public class MyReviewServiceException extends RuntimeException{
    private MyReviewServiceErrorCode myReviewServiceErrorCode;
    private String detailMessage;

    public MyReviewServiceException(MyReviewServiceErrorCode errorCode){
        super(errorCode.getMessage());
        this.myReviewServiceErrorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }
    public MyReviewServiceException(MyReviewServiceErrorCode errorCode, String detailMessage){
        super(detailMessage);
        this.myReviewServiceErrorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}
