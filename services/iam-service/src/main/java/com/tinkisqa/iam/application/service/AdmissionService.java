package com.tinkisqa.iam.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;
import com.tinkisqa.iam.domain.port.VerificationRepository;
import com.tinkisqa.iam.domain.service.AccessRules;

/**
 * Servicio de aplicación compartido: evalúa y actualiza el estado de acceso a
 * la comunidad a partir de la cuenta y su última verificación documental.
 */
@Service
public class AdmissionService {

    private final VerificationRepository verificationRepository;

    public AdmissionService(VerificationRepository verificationRepository) {
        this.verificationRepository = verificationRepository;
    }

    public AccessRules.AdmissionEvaluation evaluate(Account account) {
        VerificationState latest = verificationRepository.findLatestByAccountId(account.id())
                .map(Verification::state)
                .orElse(null);
        return AccessRules.evaluate(account, latest);
    }

    /** Devuelve la cuenta con el estado de acceso recalculado (sin persistir). */
    public Account recompute(Account account, Instant now) {
        boolean admitted = evaluate(account).admitted();
        return account.withAccessStatus(AccessRules.statusFor(admitted, account.accessStatus()), now);
    }
}
