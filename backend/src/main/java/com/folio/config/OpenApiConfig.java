package com.folio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI folioOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Folio API")
                .description("Portfolio analysis and tracking API")
                .version("1.0.0"));
    }
}