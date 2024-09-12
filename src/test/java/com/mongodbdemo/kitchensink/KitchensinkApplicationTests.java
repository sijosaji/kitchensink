package com.mongodbdemo.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class KitchensinkApplicationTests {

	@Test
	void contextLoads() {
		try (var mockedSpringApplication = mockStatic(SpringApplication.class)) {
			mockedSpringApplication.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
					.thenReturn(null);

			KitchensinkApplication.main(new String[]{});

			mockedSpringApplication.verify(() -> SpringApplication.run(KitchensinkApplication.class, new String[]{}));
		}
	}

}
