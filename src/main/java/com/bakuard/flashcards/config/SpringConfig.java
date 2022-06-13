package com.bakuard.flashcards.config;

import com.bakuard.flashcards.model.Expression;
import com.bakuard.flashcards.model.User;
import com.bakuard.flashcards.model.Word;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.UUID;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.flashcards.controller",
                "com.bakuard.flashcards.config"
        }
)
@EnableTransactionManagement
@EnableJdbcRepositories(basePackages = {"com.bakuard.flashcards.dal"})
@PropertySource("classpath:config/security.properties")
public class SpringConfig implements WebMvcConfigurer {

        @Bean
        public DataSource dataSource(Environment env) {
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
                hikariConfig.setUsername(env.getProperty("db.user"));
                hikariConfig.setPassword(env.getProperty("db.password"));
                hikariConfig.addDataSourceProperty("databaseName", env.getProperty("db.name"));
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
        public ApplicationListener<BeforeSaveEvent<?>> idGenerator() {
                return event -> {
                       if(event.getEntity() instanceof User user) user.generateIdIfAbsent();
                       else if(event.getEntity() instanceof Word word);
                       else if(event.getEntity() instanceof Expression expression);
                };
        }

}
