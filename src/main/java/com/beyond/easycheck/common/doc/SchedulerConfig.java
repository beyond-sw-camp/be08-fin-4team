package com.beyond.easycheck.common.doc;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job send3DaysBeforeReminderJob;
    private final Job send10DaysBeforeReminderJob;

    public SchedulerConfig(JobLauncher jobLauncher, Job send3DaysBeforeReminderJob, Job send10DaysBeforeReminderJob) {
        this.jobLauncher = jobLauncher;
        this.send3DaysBeforeReminderJob = send3DaysBeforeReminderJob;
        this.send10DaysBeforeReminderJob = send10DaysBeforeReminderJob;
    }

    // 3일 전 리마인더 이메일 스케줄 (매일 자정 실행)
    @Scheduled(cron = "0 0 0 * * ?")
    public void perform3DaysBeforeReminderJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(send3DaysBeforeReminderJob, jobParameters);
    }

    // 10일 전 리마인더 이메일 스케줄 (매일 자정 실행)
    @Scheduled(cron = "0 0 0 * * ?")
    public void perform10DaysBeforeReminderJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(send10DaysBeforeReminderJob, jobParameters);
    }
}