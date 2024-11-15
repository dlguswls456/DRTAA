package com.d211.drtaa.global.config.batch;

import com.d211.drtaa.domain.rent.dto.request.RentStatusRequestDTO;
import com.d211.drtaa.domain.rent.entity.Rent;
import com.d211.drtaa.domain.rent.entity.RentStatus;
import com.d211.drtaa.domain.rent.entity.car.RentCarSchedule;
import com.d211.drtaa.domain.rent.repository.RentRepository;
import com.d211.drtaa.domain.rent.repository.car.RentCarScheduleRepository;
import com.d211.drtaa.domain.rent.service.RentService;
import com.d211.drtaa.domain.user.entity.User;
import com.d211.drtaa.global.exception.rent.RentCarScheduleNotFoundException;
import com.d211.drtaa.global.util.fcm.FcmMessage;
import com.d211.drtaa.global.util.fcm.FcmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class BatchConfig {

    private final RentRepository rentRepository; // Rent 테이블을 위한 레포지토리
    private final FcmUtil fcmUtil; // FCM 알림 서비스
    private final RentService rentService;
    private final RentCarScheduleRepository rentCarScheduleRepository;

    // 렌트 예약 하루, 3일 전 알림
    @Bean
    public Job rentReservationNotificationJob(JobRepository jobRepository, Step rentReservationNotificationStep) {
        log.info("rentReservationNotificationJob");

        return new JobBuilder("rentReservationNotification", jobRepository) // 새로운 작업을 생성하는 빌더
                .incrementer(new RunIdIncrementer()) // 작업 실행 시 고유한 ID를 증가시켜주는 설정
                .start(rentReservationNotificationStep) // 작업이 시작할 스텝을 지정
                .build();
    }

    @Bean
    public Step rentReservationNotificationStep(JobRepository jobRepository, Tasklet rentReservationNotificationTasklet, PlatformTransactionManager transactionManager) {
        log.info("RentNotificationStep");

        return new StepBuilder("rentNotificationStep", jobRepository) // 새로운 스텝을 생성하는 빌더
                .tasklet(rentReservationNotificationTasklet, transactionManager) // 실행할 태스크렛과 트랜잭션 매니저를 설정
                .build(); // 스텝 객체를 생성
    }

    @Bean
    public Tasklet rentReservationNotificationTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Tasklet(1일, 3일 후 렌트 알림)");

            // 하루 뒤 예약된 렌트 정보를 조회
            log.info("하루 뒤 예약된 렌트 정보를 조회");
            LocalDateTime startDate = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59).withSecond(59);
            List<Rent> tomorrowRents = rentRepository.findByRentStartTimeBetween(startDate, endDate);

            // 3일 뒤 예약된 렌트 정보를 조회
            log.info("3일 뒤 예약된 렌트 정보를 조회");
            startDate = LocalDateTime.now().plusDays(3).withHour(0).withMinute(0).withSecond(0);
            endDate = LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59);
            List<Rent> threeDaysLaterRents = rentRepository.findByRentStartTimeBetween(startDate, endDate);

            // 알림을 보낼 렌트 목록 생성
            List<Rent> allRents = new ArrayList<>();
            allRents.addAll(tomorrowRents);
            allRents.addAll(threeDaysLaterRents);

            // 각 사용자에게 FCM 알림 전송
            for (Rent rent : allRents) {
                User user = rent.getUser(); // 사용자 정보 가져오기

                // FCM DTO 생성
                FcmMessage.FcmDTO fcmDTO = fcmUtil.makeFcmDTO("렌트 예약", rent.getTravel().getTravelName() + "의 렌트 시작이 " + (rent.getRentStartTime().isBefore(LocalDateTime.now().plusDays(3)) ? "하루" : "3일") + " 남았습니다.");
                // FCM 알림 전송
                fcmUtil.singleFcmSend(user, fcmDTO);
                log.info("사용자명: {}에게 예약 알림 전송 완료", rent.getUser().getUserNickname());
            }

            // 태스크렛 실행 완료 상태 반환
            return RepeatStatus.FINISHED;
        });
    }

    // 렌트 시작 또는 종료 1시간, 30분 전 알림
    @Bean
    public Job rentStartOrEndNotificationJob(JobRepository jobRepository, Step rentStartOrEndNotificationStep) {
        log.info("rentStartOrEndNotificationJob");

        return new JobBuilder("rentStartOrEndNotificationJob", jobRepository) // 새로운 작업을 생성하는 빌더
                .incrementer(new RunIdIncrementer()) // 작업 실행 시 고유한 ID를 증가시켜주는 설정
                .start(rentStartOrEndNotificationStep) // 작업이 시작할 스텝을 지정
                .build();
    }

    @Bean
    public Step rentStartOrEndNotificationStep(JobRepository jobRepository, Tasklet rentEndNotificationTasklet, Tasklet rentStartNotificationTasklet, PlatformTransactionManager transactionManager) {
        log.info("rentStartOrEndNotificationStep");

        return new StepBuilder("rentStartOrEndNotificationJob", jobRepository)
                .tasklet(rentStartNotificationTasklet, transactionManager)
                .tasklet(rentEndNotificationTasklet, transactionManager)
                .build();
    }

    @Bean
    public Tasklet rentStartNotificationTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Tasklet(렌트 시작 1시간, 30분 전 알림)");

            // 렌트 시작 1시간 전 알림
            log.info("렌트 시작 1시간 전 예약된 렌트 정보를 조회");
            LocalDateTime startDate = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1).withMinute(59).withSecond(59);
            List<Rent> oneHourBeforeRents = rentRepository.findByRentStartTimeBetween(startDate, endDate);
            log.info("시작 1시간 전 알림 줄 rent 개수: {}", oneHourBeforeRents.size());

            // 렌트 시작 30분 전 알림
            log.info("렌트 시작 30분 전 예약된 렌트 정보를 조회");
            startDate = LocalDateTime.now().plusMinutes(30).withSecond(0);
            endDate = LocalDateTime.now().plusMinutes(30).withSecond(59);
            List<Rent> thirtyMinutesBeforeRents = rentRepository.findByRentStartTimeBetween(startDate, endDate);
            log.info("시작 30분 전 알림 줄 rent 개수: {}", thirtyMinutesBeforeRents.size());

            // 추가된 렌트 목록에 대한 FCM 알림
            List<Rent> allRents = new ArrayList<>();
            allRents.addAll(oneHourBeforeRents);
            allRents.addAll(thirtyMinutesBeforeRents);

            for (Rent rent : allRents) {
                User user = rent.getUser();
                FcmMessage.FcmDTO fcmDTO = fcmUtil.makeFcmDTO("렌트 시작", rent.getTravel().getTravelName() + "의 렌트 시작이 " + (rent.getRentStartTime().isBefore(LocalDateTime.now().plusMinutes(30)) ? "30분" : "1시간") + " 남았습니다.");
                fcmUtil.singleFcmSend(user, fcmDTO);
                log.info("사용자명: {}에게 시작 알림 전송 완료", rent.getUser().getUserNickname());
            }

            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet rentEndNotificationTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Tasklet(렌트 종료 1시간, 30분 전 알림)");

            // 렌트 종료 1시간 전 알림
            log.info("렌트 종료 1시간 전 예약된 렌트 정보를 조회");
            LocalDateTime startDate = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1).withMinute(59).withSecond(59);
            List<Rent> oneHourBeforeRents = rentRepository.findByRentEndTimeBetween(startDate, endDate);
            log.info("종료 1시간 전 알림 줄 rent 개수: {}", oneHourBeforeRents.size());

            // 렌트 종료 30분 전 알림
            log.info("렌트 종료 30분 전 예약된 렌트 정보를 조회");
            startDate = LocalDateTime.now().plusMinutes(30).withSecond(0);
            endDate = LocalDateTime.now().plusMinutes(30).withSecond(59);
            List<Rent> thirtyMinutesBeforeRents = rentRepository.findByRentEndTimeBetween(startDate, endDate);
            log.info("종료 30분 전 알림 줄 rent 개수: {}", thirtyMinutesBeforeRents.size());

            // 추가된 렌트 목록에 대한 FCM 알림
            List<Rent> allRents = new ArrayList<>();
            allRents.addAll(oneHourBeforeRents);
            allRents.addAll(thirtyMinutesBeforeRents);

            for (Rent rent : allRents) {
                User user = rent.getUser();
                FcmMessage.FcmDTO fcmDTO = fcmUtil.makeFcmDTO("렌트 종료", rent.getTravel().getTravelName() + "의 렌트 종료가 " + (rent.getRentEndTime().isBefore(LocalDateTime.now().plusMinutes(30)) ? "30분" : "1시간") + " 남았습니다.");
                fcmUtil.singleFcmSend(user, fcmDTO);
                log.info("사용자명: {}에게 종료 알림 전송 완료", rent.getUser().getUserNickname());
            }

            return RepeatStatus.FINISHED;
        });
    }

    // 지난 일정 모두 completed로 변경
    @Bean
    public Job rentChangeStatusToCompletedJob(JobRepository jobRepository, Step rentChangeStatusToCompletedStep) {
        log.info("rentChangeStatusToCompletedJob");

        return new JobBuilder("rentChangeStatusToCompletedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(rentChangeStatusToCompletedStep)
                .build();
    }

    @Bean
    public Step rentChangeStatusToCompletedStep(JobRepository jobRepository, Tasklet rentChangeStatusToCompletedTasklet, PlatformTransactionManager transactionManager) {
        log.info("rentChangeStatusToCompletedStep");

        return new StepBuilder("rentChangeStatusToCompletedStep", jobRepository)
                .tasklet(rentChangeStatusToCompletedTasklet, transactionManager)
                .build();
    }

    @Bean
    public Tasklet rentChangeStatusToCompletedTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Tasklet(지난 일정 모두 completed로 변경)");

            // 현재 시간
            LocalDateTime now = LocalDateTime.now();
            log.info("현재 시간: {}", now);

            // 종료 시간이 지났고 상태가 'in_progress' 또는 'reserved'인 렌트들을 조회
            List<Rent> rentsToUpdate = rentRepository.findAllByRentEndTimeBeforeAndRentStatusIn(now,
                    Arrays.asList(RentStatus.in_progress, RentStatus.reserved));

            // 상태를 'completed'로 변경
            for (Rent rent : rentsToUpdate) {
                log.info("completed로 변경되는 rentId: {}", rent.getRentId());

                // 해당 렌트에 맞는 렌트 차량 스케줄 찾기
                RentCarSchedule rentCarSchedule = rentCarScheduleRepository.findByRent(rent)
                        .orElseThrow(() -> new RentCarScheduleNotFoundException("해당 렌트에 맞는 렌트 차량 스케쥴이 없습니다."));
                
                // 렌트 종료 서비스 메소드에 파라미터 전송
                rentService.rentStatusCompleted(rent, rentCarSchedule);
            }

            // 태스크렛 실행 완료 상태 반환
            return RepeatStatus.FINISHED;
        });
    }
}
