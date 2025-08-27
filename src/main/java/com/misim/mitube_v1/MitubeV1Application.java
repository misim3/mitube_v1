package com.misim.mitube_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MitubeV1Application {

	public static void main(String[] args) {
		SpringApplication.run(MitubeV1Application.class, args);
	}

}
