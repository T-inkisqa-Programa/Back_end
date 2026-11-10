package com.tinkisqa.iam.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tinkisqa.iam.application.dto.TermsDtos;
import com.tinkisqa.iam.domain.model.Terms;

/** Caso de uso: términos y condiciones vigentes. */
@Service
public class TermsService {

    private static final String VERSION = "1.0.0";
    private static final Instant UPDATED_AT = Instant.parse("2026-01-15T00:00:00Z");

    private static final Terms TERMS = new Terms(VERSION, UPDATED_AT, List.of(
            new Terms.Section("Acompañamiento seguro",
                    "T'inkisqa es un espacio de acompañamiento entre mujeres. No reemplaza la atención psicológica profesional."),
            new Terms.Section("Veracidad de los datos",
                    "Los datos que proporcionas deben ser reales. La verificación de identidad se realiza por el equipo como parte del cuidado de la comunidad."),
            new Terms.Section("Convivencia",
                    "No se permite el acoso, la discriminación ni la violencia. El equipo puede suspender cuentas que incumplan estas normas."),
            new Terms.Section("Privacidad",
                    "Tratamos tus datos con confidencialidad y solo para las finalidades descritas en esta plataforma.")));

    public TermsDtos.TermsResponse current() {
        List<TermsDtos.TermSectionDto> sections = TERMS.sections().stream()
                .map(section -> new TermsDtos.TermSectionDto(section.title(), section.body()))
                .toList();
        return new TermsDtos.TermsResponse(TERMS.version(), TERMS.updatedAt(), sections);
    }
}
