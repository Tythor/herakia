package tythor.herakia.exception;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@StandardException
public class LockedHttpException extends ResponseStatusException {
    public LockedHttpException() {
        super(HttpStatus.LOCKED);
    }

    public LockedHttpException(String reason) {
        super(HttpStatus.LOCKED, reason);
    }

    public LockedHttpException(String reason, Throwable cause) {
        super(HttpStatus.LOCKED, reason, cause);
    }
}
