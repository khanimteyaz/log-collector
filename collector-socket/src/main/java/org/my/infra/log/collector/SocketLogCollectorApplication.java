package org.my.infra.log.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class SocketLogCollectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(SocketLogCollectorApplication.class, args);
	}
}
