package com.tinkisqa.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * T'inkisqa — Content Service.
 *
 * <p>Dominio de contenido: artículos, recursos educativos y biblioteca de
 * bienestar. Servicio independiente con su propia base de datos.</p>
 */
@SpringBootApplication
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
