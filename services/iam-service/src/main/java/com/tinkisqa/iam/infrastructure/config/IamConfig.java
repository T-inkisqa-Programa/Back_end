package com.tinkisqa.iam.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Configuración transversal de infraestructura del IAM. */
@Configuration
public class IamConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /** Reloj inyectable (facilita pruebas y evita depender de la hora del sistema). */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Contrato OpenAPI del IAM.
     *
     * <p>Declara el esquema {@code bearerAuth} para que Swagger UI muestre el
     * botón <em>Authorize</strong> y permita probar los endpoints protegidos
     * pegando el token devuelto por {@code POST /api/v1/auth/login}.</p>
     */
    @Bean
    public OpenAPI iamOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("T'inkisqa — IAM Service")
                        .version("1.0.0")
                        .description("Autenticación, identidad, permisos y perfil.")
                        .contact(new Contact().name("Equipo T'inkisqa")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token de acceso obtenido en POST /api/v1/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
