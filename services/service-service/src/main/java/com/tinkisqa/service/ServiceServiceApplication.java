package com.tinkisqa.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * T'inkisqa — Service Service.
 *
 * <p>Dominio de servicios: red de apoyo, directorio de psicólogas, voluntariado
 * y postulaciones. Servicio independiente con su propia base de datos.</p>
 */
@SpringBootApplication
public class ServiceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceServiceApplication.class, args);
    }
}
