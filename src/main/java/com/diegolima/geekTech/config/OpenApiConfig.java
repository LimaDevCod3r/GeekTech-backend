package com.diegolima.geekTech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI geekTechOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GeekTech API")
                        .description("API documentation for GeekTech authentication and application endpoints.")
                        .version("v1"));
    }
}
