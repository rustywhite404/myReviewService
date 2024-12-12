package com.myreviewservice.myreviewservice.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MyReviewServiceErrorCode {

    NO_PRODUCT("상품이 존재하지 않습니다."),
    NO_REVIEWS("이 상품에 달린 리뷰가 존재하지 않습니다."),
    DUPLICATED_REVIEW("리뷰를 중복해서 등록할 수 없습니다."),
    INTERNAL_SERVER_ERROR("서버에 오류가 발생했습니다."),
    INVALID_REQUEST("잘못된 요청입니다");

    private final String message;
}
