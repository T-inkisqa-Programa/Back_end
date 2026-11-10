package com.tinkisqa.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * T'inkisqa — IAM Service (Identity and Access Management).
 *
 * <p>Responsable de: autenticación, registro, JWT, verificación de correo,
 * credenciales, roles y permisos, datos básicos del perfil y verificación de
 * identidad mediante DNI (derivada siempre a revisión humana; NUNCA se marca
 * como verificada automáticamente ni se simula una integración real).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
