package com.tinkisqa.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.port.OtpGenerator;
import com.tinkisqa.iam.application.config.IamProperties;

/**
 * Generador OTP seguro: código de 6 dígitos con {@link SecureRandom} y hash
 * HMAC-SHA256 (con la clave del JWT como sal) para no guardar el código en claro.
 *
 * <p>La misma función se usa para la huella no reversible del DNI.</p>
 */
@Component
public class SecureOtpGenerator implements OtpGenerator {

    private final SecureRandom random = new SecureRandom();
    private final byte[] hmacKey;

    public SecureOtpGenerator(IamProperties properties) {
        this.hmacKey = properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    @Override
    public String hash(String code) {
        return hmacHex("otp:" + code);
    }

    @Override
    public boolean matches(String code, String codeHash) {
        if (code == null || codeHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(code).getBytes(StandardCharsets.UTF_8),
                codeHash.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String fingerprintDocument(String dni) {
        return hmacHex("dni:" + (dni == null ? "" : dni.trim()));
    }

    private String hmacHex(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo calcular el hash HMAC.", ex);
        }
    }
}
