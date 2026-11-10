package com.tinkisqa.engagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * T'inkisqa — Engagement Service.
 *
 * <p>Dominio de engagement: rachas (acumulativas, con forma de planta),
 * notificaciones, métricas y actividad de las usuarias. Servicio independiente
 * con su propia base de datos.</p>
 */
@SpringBootApplication
public class EngagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngagementServiceApplication.class, args);
    }
}
