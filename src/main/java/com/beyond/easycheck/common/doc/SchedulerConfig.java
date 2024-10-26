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
    private final Job sendReminderEmailsJob;

    public SchedulerConfig(JobLauncher jobLauncher, Job sendReminderEmailsJob) {
        this.jobLauncher = jobLauncher;
        this.sendReminderEmailsJob = sendReminderEmailsJob;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void performBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(sendReminderEmailsJob, jobParameters);
    }
}