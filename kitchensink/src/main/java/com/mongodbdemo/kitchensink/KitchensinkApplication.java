package com.mongodbdemo.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class KitchensinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchensinkApplication.class, args);
	}

}
