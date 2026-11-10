package com.tinkisqa.iam.presentation.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.iam.application.dto.ProfileDtos;
import com.tinkisqa.iam.application.dto.VerificationDtos;
import com.tinkisqa.iam.application.service.ProfileService;
import com.tinkisqa.iam.application.service.VerificationService;
import com.tinkisqa.iam.presentation.security.CurrentUser;

import jakarta.validation.Valid;

/**
 * Verificación de identidad (DNI) y de teléfono, además del estado de acceso a
 * la comunidad. La revisión de documentos es exclusiva de moderación.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class VerificationController {

    private final VerificationService verificationService;
    private final ProfileService profileService;
    private final CurrentUser currentUser;

    public VerificationController(
            VerificationService verificationService,
            ProfileService profileService,
            CurrentUser currentUser) {
        this.verificationService = verificationService;
        this.profileService = profileService;
        this.currentUser = currentUser;
    }

    @PostMapping("/verifications")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationDtos.VerificationResponse submit(
            @Valid @RequestBody VerificationDtos.SubmitVerificationRequest request) {
        return verificationService.submit(currentUser.requireAccountId(), request);
    }

    @GetMapping("/verifications/me")
    public VerificationDtos.VerificationResponse mine() {
        return verificationService.mine(currentUser.requireAccountId());
    }

    @PostMapping("/verifications/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public VerificationDtos.VerificationResponse review(
            @PathVariable UUID id,
            @Valid @RequestBody VerificationDtos.ReviewVerificationRequest request) {
        return verificationService.review(currentUser.requireAccountId(), id, request);
    }

    @PostMapping("/phone/otp")
    public VerificationDtos.OtpSentResponse sendPhoneOtp(
            @Valid @RequestBody VerificationDtos.SendPhoneOtpRequest request) {
        return verificationService.sendPhoneOtp(currentUser.requireAccountId(), request);
    }

    @PostMapping("/phone/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPhone(@Valid @RequestBody VerificationDtos.VerifyPhoneOtpRequest request) {
        verificationService.verifyPhoneOtp(currentUser.requireAccountId(), request);
    }

    @GetMapping("/access")
    public ProfileDtos.AccessResponse access() {
        return profileService.access(currentUser.requireAccountId());
    }

    @PostMapping("/access/accept-norms")
    public ProfileDtos.AccessResponse acceptNorms() {
        return profileService.acceptNorms(currentUser.requireAccountId());
    }
}
