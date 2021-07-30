package com.lambdasys.employeeservice;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoRepositories
@EnableOpenApi
public class EmployeeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lambdasys.employeeservice.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(
                        new ApiInfoBuilder()
                                .version("1.0.0")
                                .title("Employee API")
                                .description("Documentation Employee API v1.0.0")
                                .license("Apach License Version 2.0")
                                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                                .build());
    }

    @Bean
    public MeterRegistryCustomizer meterRegistryCustomizer(MeterRegistry meterRegistry) {
        return registry -> {
            meterRegistry.config().commonTags("application", "employee");
        };
    }

}
