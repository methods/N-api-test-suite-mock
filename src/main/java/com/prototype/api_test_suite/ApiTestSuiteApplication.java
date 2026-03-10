package com.prototype.api_test_suite;

import com.prototype.api_test_suite.service.HarParserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = { // <--- KEY CHANGE HERE!
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class ApiTestSuiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiTestSuiteApplication.class, args);
	}

    @Bean
    public ApplicationRunner runHarParser(HarParserService harParserService, ApplicationArguments args) {
      return _args -> {
          String harFilePath = null;
          if (args.containsOption("har-file")) {
              harFilePath = args.getOptionValues("har-file").get(0);
          } else {
              System.out.println("No --har-file argument provided. Using default single-post-HAR-for-test.har from resources.");
              harFilePath = ApiTestSuiteApplication.class.getClassLoader().getResource("single-post-HAR-for-test.har").getPath();
          }

          if (harFilePath != null) {
              System.out.println("Processing HAR file" + harFilePath);
              harParserService.extractPostRequests(harFilePath);
          } else {
              System.err.println("HAR file path not found or specified.");
          }
      };
    }

}
