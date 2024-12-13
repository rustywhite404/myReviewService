package com.myreviewservice.myreviewservice.service;

import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewSyncService {
    private final ProductRepository productRepository;
    private final RedissonClient redissonClient;

    private static final String REDIS_REVIEW_STATS_KEY = "product:reviewStats";

    @Transactional
    public void syncReviewStatsToDatabase() {
        String key = REDIS_REVIEW_STATS_KEY;
        Map<String, Object> reviewStats = redissonClient.getMap(key); // Redis 데이터 가져오기

        // productId별로 데이터를 그룹화하여 처리
        Set<Long> productIds = new HashSet<>();
        for (String redisKey : reviewStats.keySet()) {
            String[] splitKey = redisKey.split(":");
            productIds.add(Long.parseLong(splitKey[0]));
        }

        for (Long productId : productIds) {
            // 이미 동기화된 데이터는 건너뜀
            Object syncedValue = reviewStats.get(productId + ":synced");
            if (syncedValue != null && ((Number) syncedValue).intValue() == 1) {
                continue;
            }

            // count와 sum 데이터 가져오기
            Long totalCount = ((Number) reviewStats.get(productId + ":count")).longValue();
            Long totalSum = ((Number) reviewStats.get(productId + ":sum")).longValue();
            double average = (double) totalSum / totalCount;

            // Product 업데이트
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) continue;

            product.setReviewCount(totalCount.longValue());
            product.setScore(average);

            // MySQL에 저장
            productRepository.save(product);
            log.info("MySQL 데이터베이스 동기화 완료: productId={}, reviewCount={}, averageScore={}",
                    productId, product.getReviewCount(), product.getScore());

            // 동기화 상태 플래그 설정
            reviewStats.put(productId + ":synced", 1L); // 동기화 완료 플래그
        }
    }

}
