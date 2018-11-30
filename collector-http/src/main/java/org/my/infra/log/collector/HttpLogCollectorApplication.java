package org.my.infra.log.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HttpLogCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpLogCollectorApplication.class, args);
	}
}
