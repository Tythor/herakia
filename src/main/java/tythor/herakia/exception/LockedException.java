package tythor.herakia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class LockedException extends ResponseStatusException {
    public LockedException() {
        super(HttpStatus.LOCKED);
    }

    public LockedException(String reason) {
        super(HttpStatus.LOCKED, reason);
    }
}
