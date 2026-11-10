package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.List;

/** Términos y condiciones vigentes (contenido versionado, sin persistencia). */
public record Terms(String version, Instant updatedAt, List<Section> sections) {

    public record Section(String title, String body) {
    }
}
