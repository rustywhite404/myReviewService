# 리뷰 작성 서비스

## 📝 프로젝트 소개

상품에 대한 review를 작성하고, 상품별 review 점수, 개수, 그리고 리뷰 내용을 관리합니다.

![Java 17](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring 3.x](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![MySQL 8.0](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)

<details>
    <summary>ERD 보기</summary>

![ERD](https://i.imgur.com/ghovwUq.png)

</details>

## 📝 프로젝트 실행 방법

<details>
    <summary>프로젝트 환경 설정 및 실행 가이드</summary>

유의사항 : 사전에 **Docker**와 **Docker Compose**가 설치되어 있어야 합니다.

1. 내려받은 프로젝트의 루트 경로에 `.env` 파일을 생성하여 DB 접속정보 등의 민감한 정보를 설정해주세요.설정해야 하는 항목은 아래와 같습니다.

    ```
     REDIS_PORT=6379                # Redis가 사용할 포트
     DB_USERNAME={ROOT USERNAME}    # MySQL 데이터베이스 사용자 이름
     DB_PASSWORD={ROOT PASSWORD}    # MySQL 데이터베이스 사용자 비밀번호
     MYSQL_ROOT_PASSWORD={ROOT PASSWORD} # MySQL 루트 계정 비밀번호
    ```

`.env` 파일은 개발 환경에서 사용되며, 운영 환경에서는 별도의 `prod.env` 파일을 사용할 수 있습니다.

2. **Docker Compose로 MySQL, SpringBoot, Redis 컨테이너 실행**

   프로젝트의 docker-compose.yml 파일이 위치한 경로에서 아래 명령어를 입력해주세요.

    ```
    docker-compose up --build -d
    ```

3. DB 스키마 notifyme는 컨테이너가 시작될 때 자동으로 생성됩니다.
4. 애플리케이션은 [http://localhost:8081](http://localhost:8081/)에서 실행됩니다.


</details> 

---

## 📝 API 명세서  

<details>
    <summary>리뷰 조회, 리뷰 등록 API 보기</summary>

### 리뷰 조회 API
- GET  /products/{productId}/reviews?cursor={cursor}&size={size}
- **Request Param**  
  | param | description |
  | --- | --- |
  | productId  | 상품 아이디 |
  | cursor | 커서 값 (직전 조회 API 의 응답으로 받은 cursor 값) |
  | size | 조회 사이즈 (default = 10) |
- **Response Body**
    ```json
    {
            "totalCount": 15, // 해당 상품에 작성된 총리뷰 수
            "score": 4.6, // 평균 점수
            "cursor": 6,
            "reviews": [
                    {
                            "id": 15,
                            "userId": 1, // 작성자 유저 아이디
                            "score": 5,
                            "content": "이걸 사용하고 제 인생이 달라졌습니다.",
                            "imageUrl": "/image.png",
                            "createdAt": "2024-11-25T00:00:00.000Z"
                    },
                    {
                            "id": 14,
                            "userId": 3, // 작성자 유저 아이디
                            "score": 5,
                            "content": "이걸 사용하고 제 인생이 달라졌습니다.",
                            "imageUrl": null,
                            "createdAt": "2024-11-24T00:00:00.000Z"
                    }
            ]
    }
    ``` 
  
### 리뷰 등록 API
- POST  /products/{productId}/reviews
- **Request Part**  
  - MultipartFile 타입의 단건 이미지
  - 요청부 : 
    ```json
    {
		"userId": 1,
		"score": 4,
		"content": "이걸 사용하고 제 인생이 달라졌습니다.",
    }
    ``` 

</details>  

## 📝 기술적 고민 및 구현
주요 구현 기능

- 리뷰는 존재하는 상품에만 작성 가능
- 리뷰는 '가장 최근에 작성된 리뷰' 순서대로 조회

### 유저는 하나의 상품에 대해 하나의 리뷰만 작성 가능, 1~5점 사이의 점수와 리뷰 작성 가능  
Validation을 통해 점수의 범위를 제한, DTO에서 유효성 검사를 처리하고 Entity에서는 Not null 등 데이터베이스의 책임만 가져가도록 분리

```java
public class ReviewRequestDto {
    private Long productId; //상품 ID
    private Long userId;

    @Min(value = 1, message = "Score는 최소 1이어야 합니다.")
    @Max(value = 5, message = "Score는 최대 5이어야 합니다.")
    private int score;
```

```java
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int score;
```

### 사진은 선택적으로 업로드 가능  
- 사진은 S3 에 저장된다고 가정하고, S3 적재 부분은 dummy 구현체를 생성(실제 연동X)  

추후 다른 기능에서 재사용 가능성이 있고, Review 업로드와 다소 차이가 있는 기능이므로 Service 로직에서 분리, dummy URL을 생성하는 Util에서 처리. MultipartFile 형식으로 이미지를 업로드 받아 URL을 생성하고 저장함.

```java
@Component
public class DummyS3Uploader {
    public String upload(MultipartFile file){
        log.info("Uploading file:{}",file.getOriginalFilename());
        //더미 URL 생성
        return "https://dummy-s3-url.com/"+ file.getOriginalFilename();
    }
}
```

```java
@PostMapping(value = "/{productId}/reviews", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void createReview(@PathVariable("productId") Long productId, @RequestPart("review") @Valid ReviewRequestDto requestDto,
                             @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("productId: {}, RequestDto: {}, Image: {}", productId, requestDto, image != null ? image.getOriginalFilename() : "No Image");
        reviewService.addReview(productId, requestDto, image);
    }
```

```java
public void addReview(Long productId, ReviewRequestDto requestDto, MultipartFile image) {
        (...) 
        // 이미지 파일 처리(더미 S3 구현체)
        String imageUrl = null;
        if (image != null && !image.isEmpty()) imageUrl = dummyS3Uploader.upload(image);
```

---


## 📝 기술적 고민 및 구현  

- **동시성 처리를 어떻게 하면 좋을까**  
단일 서버 환경이므로 레디스 서버를 따로 띄우는 것은 오버스펙인 것 같아서 처음에는 Optimistic Lock으로 동시성 제어를 하려 했다.  
그런데 리뷰 저장(A작업)과 리뷰 수 및 평균점수 업데이트(B작업)이 빈번하게 일어난다면 성능 문제가 생길 것 같았다. 고민 끝에 A작업이 끝난 후 레디스에서 각 상품별 리뷰, 총점을 갱신하고 일정 시간이 경과한 후 DB에 갱신된 리뷰 수와 평균을 저장하도록 처리했다.  
이 과정에서 동시성 제어 방식 역시 Optimistic Lock 락에서 Redisson을 이용한 락으로 변경했다.  
-> 시도해 볼 만한 다른 방식 : 리뷰 저장 후 이벤트 발생 시키고, 프로덕트 업데이트 리스너에서 이벤트를 처리하면 레디스 없이도 문제 해결이 가능할 것 같다. 
---  
- **인덱스는 어디에 거는 게 좋을까**  

  우선 review 테이블의 각 컬럼별 카디널리티를 확인해보았다. id와 created_at의 카디널리티가 제일 높다. 

    ```sql
    SELECT
      CONCAT(ROUND(COUNT(DISTINCT id) / COUNT(*) * 100, 2), '%') AS id_cardinality,
      CONCAT(ROUND(COUNT(DISTINCT product_id) / COUNT(*) * 100, 2), '%') AS product_id_cardinality,
      CONCAT(ROUND(COUNT(DISTINCT content) / COUNT(*) * 100, 2), '%') AS content_cardinality,
      CONCAT(ROUND(COUNT(DISTINCT user_id) / COUNT(*) * 100, 2), '%') AS user_id_cardinality,
      CONCAT(ROUND(COUNT(DISTINCT created_at) / COUNT(*) * 100, 2), '%') AS created_at_cardinality,
      CONCAT(ROUND(COUNT(DISTINCT score) / COUNT(*) * 100, 2), '%') AS score_cardinality
    FROM review;
    ```

  ![카디널리티 결과 이미지](https://i.imgur.com/V17dBEi.png)  
  - 카디널리티는 낮지만, product_id는 상품 별 리뷰 조회 시 where절에 항상 들어가는 검색 조건이므로 인덱스를 걸기에 적절한 컬럼이다. 
  - 이 프로그램은 리뷰 조회 시 `product_id`로 필터링한 뒤 `created_at DESC`로 정렬해야 하므로 조회 조건(`product_id`)과 정렬 조건(`created_at`)을 복합 인덱스로 거는 게 성능 최적화에 더 적절하다고 판단했다. 
  - JPA를 사용하고 있으므로 쿼리를 따로 관리하는 것보다는 Entity에 추가하기로 결정했다.

    ```java
    @Entity
    @Table(name = "review", indexes = {
            @Index(name = "idx_review_product_id_created_at", columnList = "product_id, created_at DESC")
    })
    @EntityListeners(AuditingEntityListener.class)
    public class Review {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        (...) 
    ```

---
- **review가 없을 때 어떤 데이터를 반환하면 좋을까?**  
   - **문제 상황 :**  
   리뷰가 없는 상품을 조회 할 때에는 어떻게 Response를 보낼 지 조건이 주어지지 않아서, 어떻게 처리하는 게 좋을 지 고민했다.
   - **고민 :**  
   처음에는 아래와 같이 Exception을 발생시키고 에러 메시지를 리턴하도록 했다.
     하지만 리뷰가 없는 게시물이라도 totalCount와 score는 결과 값으로 넘겨주어야 추후 프론트엔드 영역에서 데이터를 올바르게 처리할 수 있다.
       ```json  
       {
         "errorCode": "NO_REVIEWS",
         "errorMessage": "이 상품에 달린 리뷰가 존재하지 않습니다."
       }
       ```

   - **결과 :**  
   totalCount, score, cursor 까지는 정상적으로 넘겨주고 reviews가 없을 때에만 미리 만들어 둔 ErrorCode와 메시지를 반환하도록 처리했다.

       ```java
       if (page.isEmpty()) {
                   return ReviewResponseDto.builder()
                           .totalCount(product.getReviewCount())
                           .score(product.getScore().floatValue())
                           .cursor(cursor)
                           .reviews(Collections.emptyList())
                           .errorCode(MyReviewServiceErrorCode.NO_REVIEWS)
                           .errorMessage(MyReviewServiceErrorCode.NO_REVIEWS.getMessage())
                           .build();
               }
       ```  
     
       ```json
       {
       "totalCount": 0,
       "score": 0.0,
       "cursor": 0,
       "reviews": [],
       "errorCode": "NO_REVIEWS",
       "errorMessage": "이 상품에 달린 리뷰가 존재하지 않습니다."
       }
       ```  

---
- **테스트 코드 관리**  
IDE 내부에서 테스트 진행과 관리가 가능하도록 Http Client를 이용하여 API 통합 테스트를 진행했다.  
    ```java
    ### 리뷰 등록 API(이미지 포함 등록)
    POST http://localhost:8080/products/6/reviews
    Content-Type: multipart/form-data; boundary=----boundary
    
    ------boundary
    Content-Disposition: form-data; name="review"
    Content-Type: application/json
    
    {
      "userId": 111,
      "score": 4,
      "content": "진짜별로임!"
    }
    ------boundary
    Content-Disposition: form-data; name="image";  filename="test1.jpg";
    Content-Type: image/jpeg
    
    < C:\Users\user\Documents\test1.jpg
    ------boundary--
    
    ### 리뷰 조회 API
    GET http://localhost:8080/products/9/reviews?cursor=0&size=2
    Accept: application/json
    ```
