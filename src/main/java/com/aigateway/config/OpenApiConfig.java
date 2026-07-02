package com.aigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiGatewayOpenAPI() {
        final String jwtScheme = "bearerAuth";
        final String apiKeyScheme = "apiKeyAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AI Gateway API")
                        .version("v1")
                        .description("Multi-tenant SaaS backend that proxies LLM providers with auth, plans, rate limits and usage tracking.")
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(jwtScheme).addList(apiKeyScheme))
                .components(new Components()
                        .addSecuritySchemes(jwtScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(apiKeyScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")));
    }
}
