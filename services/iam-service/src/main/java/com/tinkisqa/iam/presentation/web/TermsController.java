package com.tinkisqa.iam.presentation.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.iam.application.dto.TermsDtos;
import com.tinkisqa.iam.application.service.TermsService;

/** Términos y condiciones vigentes (endpoint público). */
@RestController
@RequestMapping("/api/v1/terms")
public class TermsController {

    private final TermsService termsService;

    public TermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    @GetMapping
    public TermsDtos.TermsResponse terms() {
        return termsService.current();
    }
}
