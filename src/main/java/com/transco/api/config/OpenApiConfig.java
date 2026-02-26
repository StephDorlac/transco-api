package com.transco.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transcoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transcodification API")
                        .description("API REST de transcodification — résolution de règles de mapping multi-entrées")
                        .version("v1")
                        .contact(new Contact()
                                .name("Transco Team")
                                .email("contact@transco.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
