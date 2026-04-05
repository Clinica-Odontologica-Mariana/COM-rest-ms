package com.clinica.mariana.restms;

import com.clinica.mariana.restms.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RestMsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnHelloWorld() {
		HelloController controller = new HelloController();
		assertThat(controller.hello()).isEqualTo("Hello World");
	}

}
