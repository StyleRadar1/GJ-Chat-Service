package edu.dosw.styleradar.chat.domain.model;

import java.util.List;

/** A bounded page of domain results. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PageResult {
        content = List.copyOf(content);
    }
}
