package com.subtrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;


import org.springframework.context.annotation.Configuration;
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI subtrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SubTrack API")
                        .description("REST API for SubTrack "
                                + "subscription management platform. "
                                + "Tracks recurring subscriptions, "
                                + "calculates monthly burn rate, "
                                + "and provides Traffic Light "
                                + "urgency indicators.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ismail Abdi")
                                .email("ismail@subtrack.com")));
    }
}