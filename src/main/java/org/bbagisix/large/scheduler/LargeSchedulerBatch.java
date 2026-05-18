package org.bbagisix.large.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spring Batch 기반 지출 처리 스케줄러
 * expenseJob을 주기적으로 실행
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class LargeSchedulerBatch {

    private final JobLauncher jobLauncher;
    private final Job expenseJob;

    /**
     * 10분마다 Spring Batch Job 실행
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void runExpenseJob() {
        long startTime = System.currentTimeMillis();
        log.info("💨 Spring Batch scheduled start!!" + startTime);
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(expenseJob, params);
            log.info("ExpenseJob 실행 완료");
        } catch (Exception e) {
            log.error("ExpenseJob 실행 실패", e);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String formattedDuration = String.format("%d:%d:%d", hours, minutes, seconds);

        log.info("@Scheduler + Batch End [ Total time : {} ]", formattedDuration);

    }

}