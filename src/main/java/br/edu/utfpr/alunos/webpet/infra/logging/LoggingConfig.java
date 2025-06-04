package br.edu.utfpr.alunos.webpet.infra.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.ConsoleAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@RequiredArgsConstructor
public class LoggingConfig implements WebMvcConfigurer {
    
    @Value("${app.logging.format:json}")
    private String loggingFormat;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    private final CorrelationIdInterceptor correlationIdInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(correlationIdInterceptor);
    }
    
    @PostConstruct
    public void configureJsonLogging() {
        if ("json".equals(loggingFormat) && !"test".equals(activeProfile)) {
            configureStructuredLogging();
        }
    }
    
    private void configureStructuredLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        JsonLayout jsonLayout = new JsonLayout();
        jsonLayout.setContext(context);
        jsonLayout.setJsonFormatter(new JacksonJsonFormatter());
        jsonLayout.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        jsonLayout.setTimestampFormatTimezoneId("UTC");
        jsonLayout.setAppendLineSeparator(true);
        jsonLayout.start();
        
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = 
            new ConsoleAppender<>();
        appender.setContext(context);
        appender.setLayout(jsonLayout);
        appender.setName("JSON_CONSOLE");
        appender.start();
        
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(appender);
    }
    
    @Bean
    public AuditLogger auditLogger() {
        return new AuditLogger();
    }
}