package tythor.herakia.exception;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@StandardException
public class LockedException extends ResponseStatusException {
    public LockedException() {
        super(HttpStatus.LOCKED);
    }

    public LockedException(String reason) {
        super(HttpStatus.LOCKED, reason);
    }

    public LockedException(String reason, Throwable cause) {
        super(HttpStatus.LOCKED, reason, cause);
    }
}
