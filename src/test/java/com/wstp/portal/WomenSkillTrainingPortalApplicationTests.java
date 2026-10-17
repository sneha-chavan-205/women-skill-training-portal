package com.wstp.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:wstp_test;DB_CLOSE_DELAY=-1")
class WomenSkillTrainingPortalApplicationTests {

	@Test
	void contextLoads() {
	}

}
