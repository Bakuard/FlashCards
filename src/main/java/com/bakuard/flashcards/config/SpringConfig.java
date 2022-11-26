package com.bakuard.flashcards.config;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.config.security.RequestContextImpl;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.controller.message.MessagesImpl;
import com.bakuard.flashcards.dal.*;
import com.bakuard.flashcards.dal.impl.IntervalRepositoryImpl;
import com.bakuard.flashcards.dal.impl.StatisticRepositoryImpl;
import com.bakuard.flashcards.dal.impl.fragment.UserSaver;
import com.bakuard.flashcards.dal.impl.fragment.UserSaverImpl;
import com.bakuard.flashcards.dal.impl.fragment.WordOuterSourceBuffer;
import com.bakuard.flashcards.dal.impl.fragment.WordOuterSourceBufferImpl;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.service.*;
import com.bakuard.flashcards.service.util.Transaction;
import com.bakuard.flashcards.service.wordSupplementation.WordSupplementationService;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.AfterSaveEvent;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.sql.DataSource;
import javax.validation.Validator;
import java.time.Clock;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.flashcards.controller",
                "com.bakuard.flashcards.config"
        }
)
@EnableTransactionManagement
@EnableJdbcRepositories(basePackages = {"com.bakuard.flashcards.dal"})
@ConfigurationPropertiesScan
@SecurityScheme(name = "JWTScheme", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SpringConfig implements WebMvcConfigurer {

        @Bean
        public DataSource dataSource(ConfigData configData) {
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(configData.database().jdbcUrl());
                hikariConfig.setAutoCommit(false);
                hikariConfig.setMaximumPoolSize(10);
                hikariConfig.setMinimumIdle(5);
                hikariConfig.setPoolName("hikariPool");

                return new HikariDataSource(hikariConfig);
        }

        @Bean(initMethod = "migrate")
        public Flyway flyway(DataSource dataSource) {
                return Flyway.configure().
                        locations("classpath:db").
                        dataSource(dataSource).
                        load();
        }

        @Bean("transactionManager")
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
                return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public Transaction transaction(PlatformTransactionManager transactionManager) {
             return new Transaction(transactionManager);
        }

        @Bean
        public IntervalRepository intervalsRepository(JdbcTemplate jdbcTemplate) {
                return new IntervalRepositoryImpl(jdbcTemplate);
        }

        @Bean
        public StatisticRepository statisticRepository(JdbcTemplate jdbcTemplate) {
             return new StatisticRepositoryImpl(jdbcTemplate);
        }

        @Bean
        public WordOuterSourceBuffer wordSourceInfo(JdbcTemplate jdbcTemplate,
                                                    JdbcAggregateOperations jdbcAggregateOperations) {
                return new WordOuterSourceBufferImpl(jdbcTemplate, jdbcAggregateOperations);
        }

        @Bean
        public UserSaver userSaver(JdbcTemplate jdbcTemplate,
                                   JdbcAggregateOperations jdbcAggregateOperation,
                                   ConfigData configData) {
                return new UserSaverImpl(jdbcTemplate, jdbcAggregateOperation, configData);
        }

        @Bean
        public Clock clock() {
                return Clock.systemUTC();
        }

        @Bean
        public ValidatorUtil validatorUtil(Validator validator) {
                return new ValidatorUtil(validator);
        }

        @Bean
        public WordService wordService(WordRepository wordRepository,
                                       IntervalRepository intervalRepository,
                                       Clock clock,
                                       ConfigData configData,
                                       ValidatorUtil validator) {
                return new WordService(wordRepository, intervalRepository, clock, configData, validator);
        }

        @Bean
        public ExpressionService expressionService(ExpressionRepository expressionRepository,
                                                   IntervalRepository intervalRepository,
                                                   Clock clock,
                                                   ConfigData configData,
                                                   ValidatorUtil validator) {
                return new ExpressionService(expressionRepository, intervalRepository, clock, configData, validator);
        }

        @Bean(initMethod = "initialize")
        public AuthService authService(UserRepository userRepository,
                                       IntervalRepository intervalRepository,
                                       JwsService jwsService,
                                       EmailService emailService,
                                       ConfigData configData,
                                       ValidatorUtil validator) {
             return new AuthService(userRepository,
                     intervalRepository,
                     jwsService,
                     emailService,
                     configData,
                     validator);
        }

        @Bean
        public JwsService jwsService(ConfigData configData, Clock clock) {
             return new JwsService(configData, clock);
        }

        @Bean
        public EmailService emailService(ConfigData configData) {
                return new EmailService(configData);
        }

        @Bean
        public IntervalService intervalService(IntervalRepository intervalRepository) {
             return new IntervalService(intervalRepository);
        }

        @Bean
        public StatisticService statisticService(StatisticRepository statisticRepository, Clock clock) {
                return new StatisticService(statisticRepository, clock);
        }

        @Bean(initMethod = "scheduleDeleteUnusedExamples")
        public WordSupplementationService wordSupplementationService(WordRepository wordRepository,
                                                                     Clock clock,
                                                                     ObjectMapper mapper,
                                                                     ValidatorUtil validator,
                                                                     Transaction transaction) {
             return new WordSupplementationService(wordRepository, clock, mapper, validator, transaction);
        }

        @Bean
        public LocaleResolver localeResolver() {
                return new AcceptHeaderLocaleResolver();
        }

        @Bean
        public Messages messages() {
                ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
                messageSource.setBasenames("locales/exceptions", "locales/success");
                messageSource.setDefaultEncoding("UTF-8");

                return new MessagesImpl(messageSource);
        }

        @Bean
        public SortRules sortRules() {
                return new SortRules();
        }

        @Bean
        public DtoMapper dtoMapper(WordService wordService,
                                   ExpressionService expressionService,
                                   IntervalService intervalService,
                                   AuthService authService,
                                   ConfigData configData,
                                   SortRules sortRules,
                                   ValidatorUtil validator,
                                   Clock clock,
                                   Messages messages) {
                return new DtoMapper(wordService,
                        expressionService,
                        intervalService,
                        authService,
                        configData,
                        sortRules,
                        validator,
                        clock,
                        messages);
        }

        @Bean
        public RequestContext queryContext() {
                return new RequestContextImpl();
        }

        @Bean
        public ApplicationListener<BeforeConvertEvent<?>> entityCreator() {
                return event -> {
                       if(event.getEntity() instanceof Entity entity) {
                               entity.generateIdIfAbsent();
                       }
                };
        }


        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI().
                        info(
                                new Info().
                                        title("Flashcards API").
                                        version("0.1.0").
                                        contact(new Contact().email("purplespicemerchant@gmail.com"))
                        );
        }

}
