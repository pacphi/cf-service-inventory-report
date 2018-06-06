package io.pivotal.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
@EnableTask
public class ServiceInit {

	public static void main(String[] args) {
		SpringApplication.run(ServiceInit.class, args);
	}
}  