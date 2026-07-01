package com.badminton.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI badmintonOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("Badminton Management API");
        contact.setEmail("support@badminton.com");

        Info info = new Info()
                .title("Badminton Management API")
                .version("1.0")
                .contact(contact)
                .description("API for managing badminton court bookings, users, and courts");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
