package no.nav.samordning.hendelser.common.feed

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class FeedExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(BAD_REQUEST)
    fun handleException(e: ConstraintViolationException) = e.message
}
