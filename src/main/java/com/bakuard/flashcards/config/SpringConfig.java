package com.bakuard.flashcards.config;

import com.bakuard.flashcards.config.security.QueryContext;
import com.bakuard.flashcards.config.security.QueryContextImpl;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.controller.message.MessagesImpl;
import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.dal.WordsRepository;
import com.bakuard.flashcards.dal.impl.IntervalsRepositoryImpl;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.WordService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.sql.DataSource;
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
public class SpringConfig implements WebMvcConfigurer {

        @Bean
        public DataSource dataSource(ConfigData configData) {
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
                hikariConfig.setUsername(configData.databaseUser());
                hikariConfig.setPassword(configData.databasePassword());
                hikariConfig.addDataSourceProperty("databaseName", configData.databaseName());
                hikariConfig.addDataSourceProperty("portNumber", "5432");
                hikariConfig.addDataSourceProperty("serverName", "localhost");
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
        public IntervalsRepository intervalsRepository(JdbcTemplate jdbcTemplate) {
                return new IntervalsRepositoryImpl(jdbcTemplate);
        }

        @Bean
        public Clock clock() {
             return Clock.systemUTC();
        }

        @Bean
        public WordService wordService(WordsRepository wordsRepository,
                                       IntervalsRepository intervalsRepository,
                                       Clock clock) {
                return new WordService(wordsRepository, intervalsRepository, clock);
        }

        @Bean
        public ExpressionService expressionService(ExpressionRepository expressionRepository,
                                                   IntervalsRepository intervalsRepository,
                                                   Clock clock) {
                return new ExpressionService(expressionRepository, intervalsRepository, clock);
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
                                   ConfigData configData,
                                   SortRules sortRules) {
                return new DtoMapper(wordService, expressionService, configData, sortRules);
        }

        @Bean
        public QueryContext queryContext() {
                return new QueryContextImpl();
        }

        @Bean
        public ApplicationListener<BeforeConvertEvent<?>> idGenerator() {
                return event -> {
                       if(event.getEntity() instanceof Entity<?> entity) entity.generateIdIfAbsent();
                };
        }

}
