package com.somosdb.voting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI votingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Votação")
                .description("Gerenciamento de pautas, sessões e votos de associados")
                .version("v1"));
    }
}

