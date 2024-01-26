package com.bakuard.flashcards.config;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.config.security.JwsFilter;
import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.config.security.RequestContextImpl;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.controller.message.MessagesImpl;
import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.StatisticRepository;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.dal.fragment.UserSaver;
import com.bakuard.flashcards.dal.fragment.UserSaverImpl;
import com.bakuard.flashcards.dal.impl.IntervalRepositoryImpl;
import com.bakuard.flashcards.dal.impl.StatisticRepositoryImpl;
import com.bakuard.flashcards.dal.impl.WordOuterSourceBufferImpl;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.auth.policy.Access;
import com.bakuard.flashcards.model.auth.policy.Authorizer;
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.EmailService;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.IntervalService;
import com.bakuard.flashcards.service.JwsService;
import com.bakuard.flashcards.service.StatisticService;
import com.bakuard.flashcards.service.UserService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.service.wordSupplementation.WordSupplementationService;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.UUID;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.flashcards.controller",
                "com.bakuard.flashcards.config"
        }
)
@EnableWebSecurity
@EnableTransactionManagement
@EnableJdbcRepositories(basePackages = {"com.bakuard.flashcards.dal"})
@ConfigurationPropertiesScan
@SecurityScheme(name = "commonToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "registrationToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "restorePassToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "deleteToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SpringConfig implements WebMvcConfigurer {

        private static final Logger logger = LoggerFactory.getLogger(SpringConfig.class.getName());


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

        @Bean("transactionManager")
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
                return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
                return new TransactionTemplate(transactionManager);
        }

        @Bean(initMethod = "migrate")
        public Flyway flyway(DataSource dataSource) {
                return Flyway.configure().
                        locations("classpath:db").
                        dataSource(dataSource).
                        load();
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
        public WordOuterSourceBuffer wordOuterSourceBuffer(JdbcTemplate jdbcTemplate) {
                return new WordOuterSourceBufferImpl(jdbcTemplate);
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

        @Bean
        public UserService userService(UserRepository userRepository,
                                       IntervalRepository intervalRepository,
                                       ConfigData conf,
                                       ValidatorUtil validator) {
                return new UserService(userRepository,
                        intervalRepository,
                        conf,
                        validator);
        }

        @Bean()
        public AuthService authService(UserService userService,
                                       JwsService jwsService,
                                       EmailService emailService,
                                       ConfigData configData,
                                       ValidatorUtil validator) {
             return new AuthService(userService,
                     jwsService,
                     emailService,
                     configData,
                     validator);
        }

        @Bean
        public JwsService jwsService(ConfigData configData, Clock clock, ObjectMapper mapper) {
             return new JwsService(configData, clock, mapper);
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

        @Bean
        public WordSupplementationService wordSupplementationService(WordOuterSourceBuffer wordOuterSourceBuffer,
                                                                     Clock clock,
                                                                     ObjectMapper mapper,
                                                                     ValidatorUtil validator,
                                                                     TransactionTemplate transaction) {
             return new WordSupplementationService(wordOuterSourceBuffer, clock, mapper, validator, transaction);
        }

        @Bean
        public User superAdmin(UserService userService) {
                return userService.createOrReplaceSuperAdminIfNecessary();
        }

        @Bean
        public Authorizer authorizer(User superAdmin, ConfigData configData) {
                return Authorizer.newBuilder().
                        policy(request -> request.getPrincipal().
                                filter(p -> p.getId().equals(superAdmin.getId())).
                                map(p -> Access.ACCEPT).
                                orElse(Access.UNKNOWN)).
                        policy(request -> request.mapPrincipalAndResourceAndAction((p, r, a) ->
                                r.typeIs("user") &&
                                        a.nameIsOneOf("update", "delete", "getUserById") &&
                                        r.payloadIsEqualTo(p.getId()) ? Access.ACCEPT : Access.UNKNOWN
                        )).
                        policy(request -> request.mapPrincipalAndResourceAndAction((p, r, a) ->
                                r.typeIs("dictionary") &&
                                        a.nameIsOneOf("update", "findAllBy", "findById",
                                                "findByValue", "findByTranslate", "delete",
                                                "supplementNewWord", "supplementExistedWord",
                                                "jumpToCharacter") &&
                                        r.payloadIsEqualTo(p.getId()) ? Access.ACCEPT : Access.UNKNOWN
                        )).
                        policy(request -> request.mapPrincipalAndResourceAndAction((p, r, a) ->
                                r.typeIs("repetition") &&
                                        a.nameIsOneOf("findAllFromEnglishBy", "repeatFromEnglish",
                                                "findAllFromNativeBy", "repeatFromNative",
                                                "markForRepetitionFromEnglish", "markForRepetitionFromNative") &&
                                        r.payloadIsEqualTo(p.getId()) ? Access.ACCEPT : Access.UNKNOWN
                        )).
                        policy(request -> request.mapPrincipalAndResourceAndAction((p, r, a) ->
                                r.typeIs("settings") &&
                                        a.nameIsOneOf("findAllIntervals", "addInterval", "replaceInterval") &&
                                        r.payloadIsEqualTo(p.getId()) ? Access.ACCEPT : Access.UNKNOWN
                        )).
                        policy(request -> request.mapPrincipalAndResourceAndAction((p, r, a) ->
                                r.typeIs("statistic") &&
                                        a.nameIsOneOf("findStatisticForWordRepetition",
                                                "findStatisticForWordsRepetition",
                                                "findStatisticForExpressionRepetition",
                                                "findStatisticForExpressionsRepetition") &&
                                        r.payloadIsEqualTo(p.getId()) ? Access.ACCEPT : Access.UNKNOWN
                        )).
                        build();
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
        public DtoMapper dtoMapper(WordService wordService,
                                   ExpressionService expressionService,
                                   IntervalService intervalService,
                                   UserService userService,
                                   ConfigData configData,
                                   Clock clock,
                                   Messages messages) {
                return new DtoMapper(wordService,
                        expressionService,
                        intervalService,
                        userService,
                        configData,
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
                               if(entity.isNew()) entity.setId(UUID.randomUUID());
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


        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityFilterChain customFilterChain(HttpSecurity http,
                                                     JwsService jwsService,
                                                     DtoMapper mapper,
                                                     Messages messages) throws Exception {
                ObjectMapper jsonWriter = new ObjectMapper();
                jsonWriter.registerModule(new JavaTimeModule());
                jsonWriter.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return http.csrf(AbstractHttpConfigurer::disable).
                        cors(Customizer.withDefaults()).
                        sessionManagement(
                                sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        ).
                        exceptionHandling(
                                exceptionConfig -> exceptionConfig.authenticationEntryPoint((request, response, ex) -> {
                                        logger.error("Security fail", ex);

                                        response.setContentType("application/json");
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                                        jsonWriter.writeValue(
                                                response.getOutputStream(),
                                                mapper.toExceptionResponse(HttpStatus.UNAUTHORIZED,
                                                        messages.getMessage("unauthorized"))
                                        );
                                })
                        ).
                        authorizeHttpRequests(
                                authRequestConf -> authRequestConf.requestMatchers(
                                                "/api",
                                                "/apiStandardFormat/**",
                                                "/swagger-ui/**",
                                                "/users/registration/firstStep",
                                                "/users/restorePassword/firstStep",
                                                "/users/enter"
                                        ).permitAll().
                                        anyRequest().authenticated()
                        ).
                        addFilterBefore(new JwsFilter(jwsService), UsernamePasswordAuthenticationFilter.class).
                        build();
        }

}
