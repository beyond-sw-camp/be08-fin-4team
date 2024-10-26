package com.beyond.easycheck.common.doc;

import com.beyond.easycheck.mail.application.service.MailService;
import com.beyond.easycheck.reservationrooms.application.service.ReservationRoomService;
import com.beyond.easycheck.reservationrooms.infrastructure.entity.ReservationRoomEntity;
import com.beyond.easycheck.reservationrooms.infrastructure.repository.ReservationRoomRepository;
import com.beyond.easycheck.reservationrooms.ui.view.ReservationRoomView;
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

    private final ReservationRoomRepository reservationRoomRepository;
    private final ReservationRoomService reservationRoomService;
    private final MailService mailService;

    public BatchConfig(ReservationRoomRepository reservationRoomRepository, ReservationRoomService reservationRoomService, MailService mailService) {
        this.reservationRoomRepository = reservationRoomRepository;
        this.reservationRoomService = reservationRoomService;
        this.mailService = mailService;
    }

    @Bean
    public Job sendReminderEmailsJob(JobRepository jobRepository, Step sendReminderEmailsStep) {
        return new JobBuilder("sendReminderEmailsJob", jobRepository)
                .start(sendReminderEmailsStep)
                .build();
    }

    @Bean
    public Step sendReminderEmailsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendReminderEmailsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<ReservationRoomEntity> reservations = reservationRoomService.getReservationsForReminder();
                    for (ReservationRoomEntity reservation : reservations) {
                        mailService.sendReservationReminderEmail(
                                reservation.getUserEntity().getEmail(),
                                ReservationRoomView.of(reservation)
                        );
                        reservation.markReminderSent();
                        reservationRoomRepository.save(reservation);
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}