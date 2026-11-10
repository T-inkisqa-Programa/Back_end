package com.tinkisqa.iam.domain.service;

import java.util.List;

import com.tinkisqa.iam.domain.model.AccessStatus;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.IdentityStatus;
import com.tinkisqa.iam.domain.model.VerificationState;

/**
 * Evalúa y presenta el estado de revisión documental y admisión.
 *
 * <p>El acceso se otorga cuando se cumplen <strong>todas</strong> las
 * condiciones: identidad verificada por moderación, identidad de género
 * autodeclarada, normas aceptadas y titularidad del documento confirmada.
 * Este estado no bloquea las funciones de una cuenta activa.</p>
 */
public final class AccessRules {

    public record Norm(String title, String description) {
    }

    public record AdmissionCriterion(String key, String label, boolean satisfied) {
    }

    public record AdmissionEvaluation(boolean admitted, List<AdmissionCriterion> criteria) {
    }

    public static final List<Norm> COMMUNITY_NORMS = List.of(
            new Norm("Respeto", "Trata con respeto; no se toleran agresiones ni discursos de odio."),
            new Norm("Confidencialidad", "Lo que se comparte en la comunidad se queda en la comunidad."),
            new Norm("Sin juicios", "Acompaña sin juzgar la experiencia de las demás personas."),
            new Norm("Cuidado", "Pide ayuda profesional cuando la situación lo requiera."));

    private AccessRules() {
    }

    public static AdmissionEvaluation evaluate(
            boolean identidadVerificada,
            boolean autodeclaracionGenero,
            boolean normasAceptadas,
            boolean titularidadConfirmada) {

        List<AdmissionCriterion> criteria = List.of(
                new AdmissionCriterion("identidad", "Identidad verificada por el equipo", identidadVerificada),
                new AdmissionCriterion("genero", "Identidad de género autodeclarada", autodeclaracionGenero),
                new AdmissionCriterion("normas", "Normas de la comunidad aceptadas", normasAceptadas),
                new AdmissionCriterion("titularidad", "Titularidad del documento confirmada", titularidadConfirmada));

        boolean admitted = criteria.stream().allMatch(AdmissionCriterion::satisfied);
        return new AdmissionEvaluation(admitted, criteria);
    }

    /** Evalúa la admisión a partir del estado de la cuenta y su última verificación. */
    public static AdmissionEvaluation evaluate(Account account, VerificationState latestVerificationState) {
        return evaluate(
                account.identityStatus() == IdentityStatus.IDENTIDAD_VERIFICADA,
                account.genero() != null,
                account.normasAceptadas(),
                latestVerificationState == VerificationState.VERIFICADA);
    }

    public static AccessStatus statusFor(boolean admitted, AccessStatus current) {
        if (current == AccessStatus.CUENTA_SUSPENDIDA) {
            return AccessStatus.CUENTA_SUSPENDIDA;
        }
        return admitted ? AccessStatus.ACCESO_APROBADO : AccessStatus.ACCESO_PENDIENTE;
    }
}
