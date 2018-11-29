package no.nav.samordning.hendelser.feed;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FeedExceptionHandler {

    @ExceptionHandler(BadParameterException.class)
    public ResponseEntity<String> handleException(BadParameterException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
