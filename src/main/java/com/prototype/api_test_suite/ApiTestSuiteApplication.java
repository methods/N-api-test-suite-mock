package com.prototype.api_test_suite;

import com.prototype.api_test_suite.model.HarReplayResult;
import com.prototype.api_test_suite.model.HarRequest;
import com.prototype.api_test_suite.service.HarParserService;
import com.prototype.api_test_suite.service.HarReplayService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class ApiTestSuiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiTestSuiteApplication.class, args);
	}

    @Bean
    public ApplicationRunner runHarParser(
            HarParserService harParserService,
            HarReplayService harReplayService,
            ApplicationArguments args) {
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
              List<HarRequest> parsedRequests = harParserService.extractPostRequests(harFilePath);

              if (parsedRequests.isEmpty()) {
                  System.out.println("No POST requests found in HAR. Nothing to replay.");
              } else {
                  // 2. Replay the parsed requests
                  System.out.println("Replaying requests...");
                  List<HarReplayResult> replayResults = harReplayService.replayHarRequests(parsedRequests);

                  // 3. Print the results (for initial verification)
                  System.out.println("--- Replay Results ---");
                  for (HarReplayResult result : replayResults) {
                      System.out.println("  Original URL: " + result.getOriginalRequest().url());
                      System.out.println("  HTTP Status: " + result.getHttpStatus());
                      System.out.println("  Correlation ID: " + result.getCorrelationId());
                      System.out.println("  Response Body: " + result.getResponseBody());
                      System.out.println("--------------------");
                  }
              }
          } else {
              System.err.println("HAR file path not found or specified.");
          }
      };
    }
}
