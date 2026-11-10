package com.tinkisqa.iam.presentation.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import com.tinkisqa.iam.application.dto.AuthDtos;
import com.tinkisqa.iam.application.service.AuthService;
import com.tinkisqa.iam.application.service.SocialAuthService;

import jakarta.validation.Valid;

/**
 * Endpoints públicos de autenticación: registro, verificación de correo, inicio
 * de sesión, refresco de token y cierre de sesión.
 */
@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;

    public AuthController(AuthService authService, SocialAuthService socialAuthService) {
        this.authService = authService;
        this.socialAuthService = socialAuthService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.RegisterResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    public AuthDtos.AuthResponse verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-verification")
    public AuthDtos.RegisterResponse resendVerification(@Valid @RequestBody AuthDtos.ResendOtpRequest request) {
        return authService.resendVerification(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthDtos.AuthResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody AuthDtos.LogoutRequest request) {
        authService.logout(request);
    }

    @PostMapping("/social/{provider}")
    public AuthDtos.AuthResponse socialLogin(
            @org.springframework.web.bind.annotation.PathVariable String provider,
            @Valid @RequestBody AuthDtos.SocialLoginRequest request) {
        return socialAuthService.loginOrRegister(provider, request.idToken(), request.accessToken());
    }
}
