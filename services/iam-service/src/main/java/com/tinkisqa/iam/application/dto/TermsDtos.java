package com.tinkisqa.iam.application.dto;

import java.time.Instant;
import java.util.List;

/** DTOs de términos y condiciones. */
public final class TermsDtos {

    private TermsDtos() {
    }

    public record TermSectionDto(String title, String body) {
    }

    public record TermsResponse(String version, Instant updatedAt, java.util.List<TermSectionDto> sections) {
    }
}
