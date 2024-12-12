## 프로젝트 설정 및 실행 방법
유의사항 : 사전에 Docker와 Docker Compose가 설치되어 있어야 합니다.  

1. 프로젝트를 내려받아 주세요. 
   ```bash
   git clone https://github.com/rustywhite404/myReviewService.git
   cd myReviewService  
   
2. 루트 경로에 `.env` 파일을 생성하여 DB 접속정보 등의 민감한 정보를 설정해주세요.  
설정해야 하는 항목은 아래와 같습니다.  
   ```env
   DB_USERNAME={ROOT USERNAME}
   DB_PASSWORD={ROOT PASSWORD}
   MYSQL_ROOT_PASSWORD={ROOT PASSWORD}

3. **Docker Compose로 MySQL 컨테이너 실행**  
프로젝트의 docker-compose.yml 파일이 위치한 경로에서 아래 명령어를 입력해주세요. 
   ```bash
   docker-compose up --build -d

3. DB 스키마 myReviewService는 컨테이너가 시작될 때 자동으로 생성됩니다.   
 
4. 애플리케이션은 http://localhost:8080 에서 실행됩니다. 

---

## 기술적 고민 및 구현  

- **동시성 처리를 어떻게 하면 좋을까**  
우선 Redis를 이용한 방법과 DB 레벨에서 락을 제어하는 방법 중에서는 DB 락을 사용하기로 했습니다.  
단일 서버 환경이므로 이 기능만을 위해서 레디스 서버를 따로 띄우는 것은 오버스펙인 것 같아서입니다.  
다음으로, DB락 중에서는 동시에 수천명이 하나의 상품에 리뷰를 다는 경우는 많지 않으므로 Optimistic Lock을 거는 게 나을 지, 
데이터 정합성을 생각하여 Pessimistic Lock을 거는 게 좋을 지 고민중입니다.  

---  
- **인덱스는 어디에 거는 게 좋을까**  

  우선 review 테이블의 각 컬럼별 카디널리티를 확인해보았습니다. id와 created_at의 카디널리티가 제일 높습니다. 

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
  - 카디널리티는 낮지만, product_id는 상품 별 리뷰 조회 시 where절에 항상 들어가는 검색 조건이므로 인덱스를 걸기에 적절한 컬럼입니다. 
  - 이 프로그램은 리뷰 조회 시 `product_id`로 필터링한 뒤 `created_at DESC`로 정렬해야 하므로 조회 조건(`product_id`)과 정렬 조건(`created_at`)을 복합 인덱스로 거는 게 성능 최적화에 더 적절하다고 판단했습니다. 
  - JPA를 사용하고 있으므로 쿼리를 따로 관리하는 것보다는 Entity에 추가하기로 결정했습니다.

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
   리뷰가 없는 상품을 조회 할 때에는 어떻게 Response를 보낼 지 조건이 주어지지 않아서, 어떻게 처리하는 게 좋을 지 고민했습니다.
   - **고민 :**  
   처음에는 아래와 같이 Exception을 발생시키고 에러 메시지를 리턴하도록 했습니다.
     하지만 리뷰가 없는 게시물이라도 totalCount와 score는 결과 값으로 넘겨주어야 추후 프론트엔드 영역에서 데이터를 올바르게 처리할 수 있습니다.
       ```json  
       {
         "errorCode": "NO_REVIEWS",
         "errorMessage": "이 상품에 달린 리뷰가 존재하지 않습니다."
       }
       ```

   - **결과 :**  
   totalCount, score, cursor 까지는 정상적으로 넘겨주고 reviews가 없을 때에만 미리 만들어 둔 ErrorCode와 메시지를 반환하도록 처리했습니다.

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

