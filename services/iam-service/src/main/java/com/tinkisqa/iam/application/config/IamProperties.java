package com.tinkisqa.iam.application.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la capa de aplicación del IAM: secreto/emisor del JWT y
 * políticas de tokens y OTP. Se enlaza desde el prefijo {@code tinkisqa.*}.
 */
@ConfigurationProperties(prefix = "tinkisqa")
public class IamProperties {

    private final Security security = new Security();
    private final Iam iam = new Iam();

    public Security getSecurity() {
        return security;
    }

    public Iam getIam() {
        return iam;
    }

    public static class Security {

        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }

        public static class Jwt {

            private String secret = "change_me_dev_secret_with_at_least_32_chars";
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

    public static class Iam {

        private Duration accessTokenTtl = Duration.ofHours(1);
        private Duration refreshTokenTtl = Duration.ofDays(7);
        private final Otp otp = new Otp();
        private final Seed seed = new Seed();

        public Seed getSeed() {
            return seed;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }

        public Otp getOtp() {
            return otp;
        }

        public static class Otp {

            private Duration ttl = Duration.ofMinutes(10);
            private int maxAttempts = 5;
            private Duration resendCooldown = Duration.ofMinutes(1);

            public Duration getTtl() {
                return ttl;
            }

            public void setTtl(Duration ttl) {
                this.ttl = ttl;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Duration getResendCooldown() {
                return resendCooldown;
            }

            public void setResendCooldown(Duration resendCooldown) {
                this.resendCooldown = resendCooldown;
            }
        }

        /**
         * Datos de prueba para desarrollo. Desactivado por defecto: solo se
         * activa explícitamente con {@code tinkisqa.iam.seed.enabled=true}
         * (por ejemplo desde {@code docker-compose.yml}). Nunca debe activarse
         * en producción.
         */
        public static class Seed {

            private boolean enabled = false;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }
}
