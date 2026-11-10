package com.tinkisqa.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades transversales del Gateway (seguridad, CORS y rutas públicas).
 * Se enlazan desde el prefijo {@code tinkisqa.*} de application.yml.
 */
@ConfigurationProperties(prefix = "tinkisqa")
public class TinkisqaProperties {

    private final Security security = new Security();
    private final Cors cors = new Cors();
    private final GatewaySettings gateway = new GatewaySettings();

    public Security getSecurity() {
        return security;
    }

    public Cors getCors() {
        return cors;
    }

    public GatewaySettings getGateway() {
        return gateway;
    }

    public static class Security {

        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }

        public static class Jwt {

            /** Secreto compartido (HMAC-SHA256). Debe tener al menos 32 caracteres. */
            private String secret = "change_me_dev_secret_with_at_least_32_chars";

            /** Emisor esperado en el claim {@code iss}. */
            private String issuer = "tinkisqa-iam";

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public String getIssuer() {
                return issuer;
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer;
            }
        }
    }

    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class GatewaySettings {

        private List<String> publicPaths = new ArrayList<>();

        public List<String> getPublicPaths() {
            return publicPaths;
        }

        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }
    }
}
