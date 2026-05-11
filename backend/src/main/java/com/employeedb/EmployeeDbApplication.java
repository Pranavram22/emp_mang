package com.employeedb;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmployeeDbApplication {

  public static void main(String[] args) {
    SpringApplication.run(EmployeeDbApplication.class, args);
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info().title("Employee Database API").version("1.0"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
  }
}
