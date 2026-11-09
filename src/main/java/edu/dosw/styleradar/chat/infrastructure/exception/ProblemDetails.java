package edu.dosw.styleradar.chat.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/** Factory of RFC 7807 problem details with the project error types. */
public final class ProblemDetails {
    public static final String TYPE_BASE = "https://styleradar.dosw/errores/";

    private ProblemDetails() {
    }

    public static ProblemDetail of(HttpStatus status, String type, String title, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setTitle(title);
        if (path != null) {
            problem.setInstance(URI.create(path));
        }
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
