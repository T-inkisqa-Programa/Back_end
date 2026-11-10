package com.tinkisqa.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * T'inkisqa — Social Service.
 *
 * <p>Dominio social: comunidades, foros, publicaciones, comentarios, likes,
 * guardados y testimonios. Servicio independiente con su propia base de datos.</p>
 */
@SpringBootApplication
public class SocialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}
