package com.d211.drtaa.domain.rent.job;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class RentScheduledJob {

    private final JobLauncher jobLauncher;
    private final Job rentReservationNotificationJob;
    private final Job rentStartOrEndNotificationJob;

//    @Scheduled(cron = "0 0/10 * * * *") // 매 10분마다 실행
//    public void checkEvery10Minutes() throws Exception {
//        log.info("10분마다 실행되는 메소드");
//    }
//
//    @Scheduled(cron = "0 0,30 * * * *") // 매 시간 0분과 30분에 실행
//    public void checkEvery30Minutes() throws Exception {
//        log.info("30분마다 실행되는 메소드");
//    }

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시에 실행
    public void rentReservationNotificationJob() {
        log.info("매일 오전 9시에 시작되는 Job");
        try {
            log.info("Rent Reservation Notification Job Started");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(rentReservationNotificationJob, jobParameters);
        } catch (Exception e) {
            // 예외 처리
            log.error("Rent Reservation Notification Job execution failed", e);
        }
    }

    @Scheduled(cron =  "0 0,30 * * * *") // 30분마다 실행
    public void rentStartOrEndNotificationJob() {
        log.info("30분마다 실행되는 Job");
        try {
            log.info("Rent Start or End Notification Job Started");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(rentStartOrEndNotificationJob, jobParameters);
        } catch (Exception e) {
            // 예외 처리
            log.error("Rent Start or End Notification Job execution failed", e);
        }
    }
}