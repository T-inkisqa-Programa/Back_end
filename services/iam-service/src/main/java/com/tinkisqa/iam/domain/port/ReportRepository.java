package com.tinkisqa.iam.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.Report;
import com.tinkisqa.iam.domain.model.ReportStatus;

/** Puerto de salida: persistencia de denuncias. */
public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    /** @param status {@code null} para listar todas. */
    List<Report> findByStatus(ReportStatus status, int page, int size);

    /** @param status {@code null} para contar todas. */
    long count(ReportStatus status);
}
