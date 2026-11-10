package com.tinkisqa.social.application.dto;

import java.util.List;

/** Página genérica de resultados. */
public record PageResponse<T>(List<T> items, long total, int page, int size) {
}
