package com.beyond.easycheck;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableBatchProcessing
public class EasycheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasycheckApplication.class, args);
    }

}
