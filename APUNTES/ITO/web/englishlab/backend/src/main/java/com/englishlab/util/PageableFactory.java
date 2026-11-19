package com.englishlab.util;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Construye el {@link Pageable} a partir de los query params de paginación,
 * aplicando: page 0-based, size (default 20, tope 100) y sort con lista blanca.
 *
 * <p>El campo {@code sort} se espera como {@code campo} o {@code campo,asc|desc}.
 * Si el campo no está en la lista blanca se usa el campo por defecto.
 */
public final class PageableFactory {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PageableFactory() {
    }

    public static Pageable of(Integer page, Integer size, String sort, Set<String> allowedFields, String defaultField) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null ? DEFAULT_SIZE : size;
        s = Math.min(Math.max(s, 1), MAX_SIZE);

        String field = defaultField;
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String candidate = parts[0].trim();
            if (allowedFields.contains(candidate)) {
                field = candidate;
                if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                    direction = Sort.Direction.DESC;
                }
            }
        }
        return PageRequest.of(p, s, Sort.by(direction, field));
    }
}