package com.bookurmedical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BookurmedicalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookurmedicalApplication.class, args);
	}

}
