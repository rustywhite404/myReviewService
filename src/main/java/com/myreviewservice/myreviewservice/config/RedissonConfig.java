package com.myreviewservice.myreviewservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${redis.address:redis://127.0.0.1:6379}")
    private String redisAddress;

    @Bean
    public RedissonClient redissonClient() {
        System.out.println("-----------reids-address check:"+redisAddress);
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisAddress) // Redis 서버 주소
                .setConnectionPoolSize(50) // 커넥션 풀 크기
                .setConnectionMinimumIdleSize(10) // 최소 유휴 연결 수
                .setRetryAttempts(3) // 재시도 횟수
                .setRetryInterval(1500); // 재시도 간격(ms)

        // JSON 직렬화 설정 - Redis 서버에서 데이터를 조회해도 바이너리로 나오지 않도록
        config.setCodec(new CustomJsonJacksonCodec());

        return Redisson.create(config);
    }
}
