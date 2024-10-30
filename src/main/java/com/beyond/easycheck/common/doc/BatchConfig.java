package com.beyond.easycheck.common.doc;

import com.beyond.easycheck.mail.application.service.MailService;
import com.beyond.easycheck.reservationrooms.application.service.ReservationRoomService;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
import com.beyond.easycheck.rooms.application.service.RoomService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final ReservationRoomService reservationRoomService;
    private final MailService mailService;
    private final RoomService roomService;

    public BatchConfig(ReservationRoomService reservationRoomService, MailService mailService, RoomService roomService) {
        this.reservationRoomService = reservationRoomService;
        this.mailService = mailService;
        this.roomService = roomService;
    }

    @Bean
    public Job sendReminderEmailsJob(JobRepository jobRepository, Step send3DaysReminderEmailsStep, Step send10DaysReminderEmailsStep) {
        return new JobBuilder("sendReminderEmailsJob", jobRepository)
                .start(send3DaysReminderEmailsStep)
                .next(send10DaysReminderEmailsStep)
                .build();
    }

    @Bean
    public Job initializeTwoMonthsAheadAvailabilityJob(JobRepository jobRepository, Step initializeTwoMonthsAheadAvailabilityStep) {
        return new JobBuilder("initializeTwoMonthsAheadAvailabilityJob", jobRepository)
                .start(initializeTwoMonthsAheadAvailabilityStep)
                .build();
    }

    @Bean
    public Step send3DaysReminderEmailsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("send3DaysReminderEmailsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<ReservationRoomEntity> reservations = reservationRoomService.get3DaysBeforeReservationsForReminder();
                    for (ReservationRoomEntity reservation : reservations) {
                        mailService.send3DaysBeforeReservationReminderEmail(
                                reservation.getUserEntity().getEmail(),
                                ReservationRoomView.of(reservation)
                        );
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step send10DaysReminderEmailsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("send10DaysReminderEmailsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<ReservationRoomEntity> reservations = reservationRoomService.get10DaysBeforeReservationsForReminder();
                    for (ReservationRoomEntity reservation : reservations) {
                        mailService.send10DaysBeforeReservationReminderEmail(
                                reservation.getUserEntity().getEmail(),
                                ReservationRoomView.of(reservation)
                        );
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step initializeTwoMonthsAheadAvailabilityStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("initializeTwoMonthsAheadAvailabilityStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    roomService.initializeTwoMonthsAheadAvailability();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}