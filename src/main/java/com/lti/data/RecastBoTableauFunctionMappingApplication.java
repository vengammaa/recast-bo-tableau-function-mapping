package com.lti.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@SpringBootApplication
@EnableEurekaClient
public class RecastBoTableauFunctionMappingApplication {

	public static void main(String[] args)  {
		SpringApplication.run(RecastBoTableauFunctionMappingApplication.class, args);
		
	}

}
