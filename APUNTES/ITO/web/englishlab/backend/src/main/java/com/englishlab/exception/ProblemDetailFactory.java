package com.englishlab.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Fabrica los ProblemDetail JSON uniformes que devuelven tanto el @ControllerAdvice
 * como los AuthenticationEntryPoint / AccessDeniedHandler.
 */
public final class ProblemDetailFactory {

    private ProblemDetailFactory() {
    }

    public static ProblemDetail create(HttpStatus status, String message, String path) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(path == null ? null : URI.create(path));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("error", status.getReasonPhrase());
        pd.setProperty("message", message);
        pd.setProperty("path", path);
        pd.setProperty("fieldErrors", List.of());
        return pd;
    }
}
