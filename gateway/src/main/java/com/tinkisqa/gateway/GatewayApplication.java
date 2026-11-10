package com.tinkisqa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del API Gateway de T'inkisqa.
 *
 * <p>Es el único servicio expuesto al exterior. Se encarga del enrutamiento
 * hacia los microservicios, de CORS y de los controles transversales de
 * seguridad (validación de JWT y propagación de la identidad).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
