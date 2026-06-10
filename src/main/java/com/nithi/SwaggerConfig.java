package com.nithi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI nithiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Nithi My Assistant API")
                .description(
                    "AI Agent REST API — " +
                    "Chat, Email, Calls, Food Ordering")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Nithisha Bajjuri")
                    .email("nithishabajjuri218@gmail.com")));
    }
}
