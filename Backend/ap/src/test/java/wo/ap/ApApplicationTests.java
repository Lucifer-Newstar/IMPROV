package wo.ap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the application context starts.
 *
 * Runs against in-memory H2 (see src/test/resources/application-test.properties)
 * so it needs no running database — which is what allows CI to pass.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApApplicationTests {

	@Test
	void contextLoads() {
	}

}
