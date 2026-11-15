package com.meufreela.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MeufreelaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeufreelaApiApplication.class, args);
	}

}
