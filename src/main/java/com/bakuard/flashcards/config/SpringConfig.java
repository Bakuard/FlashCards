package com.bakuard.flashcards.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.nutritionManager.controller",
                "com.bakuard.nutritionManager.config"
        }
)
@EnableTransactionManagement
@EnableScheduling
public class SpringConfig implements WebMvcConfigurer {



}
