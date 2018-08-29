package ao.jpaQueryHelper;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpaQueryHelperApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(JpaQueryHelperApplication.class, args);
		
		
		System.in.read();
		
	}
}
