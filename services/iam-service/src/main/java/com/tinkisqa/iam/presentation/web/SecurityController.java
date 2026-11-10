package com.tinkisqa.iam.presentation.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.iam.application.dto.SecurityDtos;
import com.tinkisqa.iam.application.service.SecurityService;
import com.tinkisqa.iam.presentation.security.CurrentUser;

import jakarta.validation.Valid;

/** Bitácora de seguridad, reportes y bloqueos. */
@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    private final SecurityService securityService;
    private final CurrentUser currentUser;

    public SecurityController(SecurityService securityService, CurrentUser currentUser) {
        this.securityService = securityService;
        this.currentUser = currentUser;
    }

    @GetMapping("/events")
    public List<SecurityDtos.SecurityEventResponse> events() {
        return securityService.events(currentUser.requireAccountId());
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void report(@Valid @RequestBody SecurityDtos.ReportRequest request) {
        securityService.report(currentUser.requireAccountId(), request);
    }

    @PostMapping("/blocks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void block(@Valid @RequestBody SecurityDtos.BlockRequest request) {
        securityService.block(currentUser.requireAccountId(), request);
    }
}
