package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.Report;
import com.tinkisqa.iam.domain.model.ReportStatus;
import com.tinkisqa.iam.domain.port.ReportRepository;

/** Adaptador que implementa {@link ReportRepository} sobre JPA. */
@Component
public class ReportRepositoryAdapter implements ReportRepository {

    private final ReportJpaRepository repository;

    public ReportRepositoryAdapter(ReportJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Report save(Report report) {
        return toDomain(repository.save(toEntity(report)));
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return repository.findById(id).map(ReportRepositoryAdapter::toDomain);
    }

    @Override
    public List<Report> findByStatus(ReportStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(statusSpec(status), pageRequest).getContent().stream()
                .map(ReportRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public long count(ReportStatus status) {
        return repository.count(statusSpec(status));
    }

    private static Specification<ReportJpaEntity> statusSpec(ReportStatus status) {
        if (status == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static ReportJpaEntity toEntity(Report report) {
        ReportJpaEntity entity = new ReportJpaEntity();
        entity.setId(report.id());
        entity.setReporterId(report.reporterId());
        entity.setTargetType(report.targetType());
        entity.setTargetId(report.targetId());
        entity.setReason(report.reason());
        entity.setStatus(report.status());
        entity.setReviewerId(report.reviewerId());
        entity.setResolution(report.resolution());
        entity.setCreatedAt(report.createdAt());
        entity.setResolvedAt(report.resolvedAt());
        return entity;
    }

    private static Report toDomain(ReportJpaEntity entity) {
        return new Report(
                entity.getId(),
                entity.getReporterId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getReviewerId(),
                entity.getResolution(),
                entity.getCreatedAt(),
                entity.getResolvedAt());
    }
}
