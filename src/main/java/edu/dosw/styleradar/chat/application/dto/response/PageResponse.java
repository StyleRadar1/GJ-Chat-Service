package edu.dosw.styleradar.chat.application.dto.response;

import edu.dosw.styleradar.chat.domain.model.PageResult;

import java.util.List;
import java.util.function.Function;

/** Stable REST envelope for paginated results. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PageResponse {
        content = List.copyOf(content);
    }

    public static <S, T> PageResponse<T> map(PageResult<S> source, Function<S, T> mapper) {
        return new PageResponse<>(source.content().stream().map(mapper).toList(), source.page(), source.size(),
                source.totalElements(), source.totalPages());
    }
}
