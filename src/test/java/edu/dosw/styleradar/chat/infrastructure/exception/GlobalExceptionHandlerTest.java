package edu.dosw.styleradar.chat.infrastructure.exception;

import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsConstraintViolationAndIllegalArgumentTo400() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/conversations");
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("size debe ser <= 100");

        ProblemDetail constraintProblem = handler.constraintViolation(
                new ConstraintViolationException(Set.of(violation)), request);
        assertThat(constraintProblem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(constraintProblem.getDetail()).contains("size debe ser <= 100");

        ProblemDetail illegalArgProblem = handler.illegalArgument(
                new IllegalArgumentException("page debe ser >= 0"), request);
        assertThat(illegalArgProblem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(illegalArgProblem.getDetail()).isEqualTo("page debe ser >= 0");
    }

    @Test
    void mapsMethodNotSupportedTo405AndStoreNotFoundTo404() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/stores/900/conversations");

        ProblemDetail methodProblem = handler.methodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"), request);
        assertThat(methodProblem.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());

        ProblemDetail storeNotFound = handler.notFound(new StoreNotFoundException("No existe"), request);
        assertThat(storeNotFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
