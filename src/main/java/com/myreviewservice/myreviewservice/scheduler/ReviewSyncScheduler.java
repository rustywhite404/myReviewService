package com.myreviewservice.myreviewservice.scheduler;

import com.myreviewservice.myreviewservice.service.ReviewSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewSyncScheduler {
    private final ReviewSyncService reviewSyncService;

    private boolean isRunning = false;
    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void syncReviewStats() {
        //중복실행방지
        if(isRunning){
            return; //이미 실행중이면 종료
        }
        isRunning = true;
        try {
            reviewSyncService.syncReviewStatsToDatabase();
            log.info("-----동기화 작업 실행:"+System.currentTimeMillis());
        }finally {
            isRunning = false;
        }

    }
}
