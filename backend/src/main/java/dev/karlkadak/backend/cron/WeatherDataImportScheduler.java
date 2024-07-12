package dev.karlkadak.backend.cron;

import dev.karlkadak.backend.service.WeatherDataImporter;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Used for scheduling the weather data importing using
 * {@link dev.karlkadak.backend.service.WeatherDataImporter WeatherDataImporter} and
 * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler ThreadPoolTaskScheduler}<br> Also logs the
 * scheduling of imports using {@link java.util.logging.Logger}
 */
@Component
public class WeatherDataImportScheduler {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final WeatherDataImporter weatherDataImporter;
    private final Logger logger;

    /**
     * Cron expression gathered from application.properties based on which {@link #taskScheduler} runs
     * {@link dev.karlkadak.backend.service.WeatherDataImporter#defaultImport WeatherDataImporter.defaultImport}
     */
    @Value("${importer.cron.expression}")
    @Setter
    private String importerCronExpression;

    public WeatherDataImportScheduler(ThreadPoolTaskScheduler taskScheduler, WeatherDataImporter weatherDataImporter,
                                      Logger logger) {
        this.taskScheduler = taskScheduler;
        this.weatherDataImporter = weatherDataImporter;
        this.logger = logger;
    }

    /**
     * Schedules the
     * {@link dev.karlkadak.backend.service.WeatherDataImporter#defaultImport WeatherDataImporter.defaultImport} method
     * to run using the cron expression specified in application.properties
     */
    @PostConstruct
    public void scheduleImport() {
        taskScheduler.schedule(weatherDataImporter::defaultImport, new CronTrigger(importerCronExpression));
        logger.info("Scheduled weather data fetching.");
    }
}
