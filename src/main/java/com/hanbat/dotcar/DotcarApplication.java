package com.hanbat.dotcar;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DotcarApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_name", dotenv.get("DB_name"));
		System.setProperty("DB_driver", dotenv.get("DB_driver"));
		System.setProperty("DB_url", dotenv.get("DB_url"));
		System.setProperty("DB_username", dotenv.get("DB_username"));
		System.setProperty("DB_password", dotenv.get("DB_password"));

		System.setProperty("pre_signed_url", dotenv.get("pre_signed_url"));

		SpringApplication.run(DotcarApplication.class, args);
	}

}
