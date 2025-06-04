package br.edu.utfpr.alunos.webpet.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @Bean
    public OpenAPI webPetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WebPet API")
                        .description("API para gerenciamento de adoção de animais")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("WebPet Team")
                                .email("contato@webpet.com")
                                .url("https://github.com/WebPetFabrica/web-pet-back"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://api.webpet.com")
                                .description("Ambiente de Produção")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }
}