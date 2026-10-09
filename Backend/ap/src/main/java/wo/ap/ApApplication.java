package wo.ap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class ApApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApApplication.class, args);
	}

	/**
	 * The one clock the app reads time from. Never call Instant.now() or
	 * LocalDate.now() directly in services — always go through this bean, so
	 * tests can pin time and day boundaries are computed in the user's
	 * timezone (D2).
	 */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

}
