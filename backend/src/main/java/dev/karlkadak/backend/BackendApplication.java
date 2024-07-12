package dev.karlkadak.backend;

import dev.karlkadak.backend.cron.WeatherDataImportScheduler;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public ApplicationRunner initializeWeatherDataImport(WeatherDataImportScheduler weatherDataImportScheduler) {
		return _ -> weatherDataImportScheduler.scheduleImport();
	}
}
