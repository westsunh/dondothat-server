package org.bbagisix.large.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bbagisix.large.service.LargeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지출 파일 처리 스케줄러
 * LargeServiceImpl을 통해 파일 기반 배치 실행
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class LargeScheduler {

    private final LargeService largeService; // 서비스 주입

    /**
     * 10분마다 지출 내역 파일을 읽어 DB에 저장
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void runExpenseFileJob() {
        long startTime = System.currentTimeMillis();
        log.info("--- start ---" + startTime);
        try {
            largeService.processExpenseFile();
        } catch (Exception e) {
            log.error("지출 내역 배치 작업 실패", e);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        long totalSeconds = durationMs / 1000; 
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String formattedDuration = String.format("%d:%d:%d", hours, minutes, seconds);
        log.info("@Scheduler + indexing End [ Total time : {} ]", formattedDuration);
    }

}