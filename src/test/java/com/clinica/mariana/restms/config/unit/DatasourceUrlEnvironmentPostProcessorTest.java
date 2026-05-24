package com.clinica.mariana.restms.config.unit;

import com.clinica.mariana.restms.config.DatasourceUrlEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DatasourceUrlEnvironmentPostProcessorTest {

	private final DatasourceUrlEnvironmentPostProcessor postProcessor = new DatasourceUrlEnvironmentPostProcessor();

	@Test
	void shouldConvertPostgresqlUrlToJdbcUrl() {
		MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.url",
				"postgresql://postgres:secret@aws-1-sa-east-1.pooler.supabase.com:6543/postgres");

		postProcessor.postProcessEnvironment(environment, null);

		assertThat(environment.getProperty("spring.datasource.url")).isEqualTo(
				"jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres?user=postgres&password=secret");
		assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("postgres");
		assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("secret");
	}

	@Test
	void shouldKeepJdbcUrlUnchanged() {
		MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.url",
				"jdbc:postgresql://localhost:5432/postgres");

		postProcessor.postProcessEnvironment(environment, null);

		assertThat(environment.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:postgresql://localhost:5432/postgres");
	}
}
