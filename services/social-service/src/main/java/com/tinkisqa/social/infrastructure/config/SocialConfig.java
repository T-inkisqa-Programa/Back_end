package com.tinkisqa.social.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Configuración transversal de infraestructura del servicio social. */
@Configuration
public class SocialConfig {

    /** Reloj inyectable (facilita pruebas y evita depender de la hora del sistema). */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Contrato OpenAPI del servicio social.
     *
     * <p>Declara el esquema {@code bearerAuth} para que Swagger UI muestre el
     * botón <em>Authorize</em> con el token emitido por el IAM Service.</p>
     */
    @Bean
    public OpenAPI socialOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("T'inkisqa — Social Service")
                        .version("1.0.0")
                        .description("Comunidades, solicitudes de ingreso, publicaciones, comentarios e interacciones.")
                        .contact(new Contact().name("Equipo T'inkisqa")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token de acceso obtenido en POST /api/v1/auth/login del IAM.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
