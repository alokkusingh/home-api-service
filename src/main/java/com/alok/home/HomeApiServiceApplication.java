package com.alok.home;

import com.alok.home.commons.utils.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan({
		"com.alok.home.config",
		"com.alok.home.mqtt.config",
		"com.alok.home.commons.security.properties"
})
@EnableScheduling
@SpringBootApplication(
		scanBasePackages = {
				"com.alok.home",
				"com.alok.home.commons.exception",
				"com.alok.home.commons.security",
				"com.alok.home.commons.entity",
				"com.alok.home.commons.repository",
				"com.alok.home.commons.utils",
				"com.alok.home.commons.utils.annotations"
		}
)
@Slf4j
public class HomeApiServiceApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(HomeApiServiceApplication.class, args);
	}

	//@LogExecutionTime
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");
		System.out.println("Application Started!!!");
	}
}
