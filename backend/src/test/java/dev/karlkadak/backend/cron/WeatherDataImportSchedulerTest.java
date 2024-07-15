package dev.karlkadak.backend.cron;

import dev.karlkadak.backend.service.WeatherDataImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class WeatherDataImportSchedulerTest {

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private WeatherDataImporter weatherDataImporter;

    @Mock
    private Logger logger;

    @InjectMocks
    private WeatherDataImportScheduler weatherDataImportScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherDataImportScheduler = new WeatherDataImportScheduler(taskScheduler, weatherDataImporter, logger);
    }

    @Test
    void testScheduleImport() {
        weatherDataImportScheduler.setImporterCronExpression("*/5 * * * * *");
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        doReturn(mockFuture).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        weatherDataImportScheduler.scheduleImport();

        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(CronTrigger.class));
        verify(logger, times(1)).info(anyString());
    }
}