package com.tinkisqa.iam.domain.port;

import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.OtpChallenge;

/** Puerto de salida: persistencia de desafíos OTP (correo y SMS). */
public interface OtpChallengeRepository {

    OtpChallenge save(OtpChallenge challenge);

    Optional<OtpChallenge> findLatestActive(String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose);

    Optional<UUID> findLatestId(String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose);

    void invalidateAll(String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose);
}
