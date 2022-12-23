package com.alok.home;

import com.alok.home.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan({"com.alok.spring.mqtt.config", "com.alok.spring.config"})
@EnableScheduling
@SpringBootApplication
@Slf4j
public class HomeApiServiceApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(HomeApiServiceApplication.class, args);
	}

	@LogExecutionTime
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");
		System.out.println("Application Started!!!");
	}
}
