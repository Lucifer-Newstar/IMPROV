package wo.ap;

import org.springframework.http.HttpStatus;

/**
 * Application exception carrying the HTTP status it should produce.
 * Mapped to an RFC 7807 problem document by {@link ApiExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
