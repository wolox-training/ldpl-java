package wolox.training.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ErrorConstants.ENTITY_NOT_FOUND, new HttpHeaders(),
            HttpStatus.NOT_FOUND,
            request);
    }

    @ExceptionHandler({BookIdMismatchException.class, ConstraintViolationException.class,
        DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handleIdMismatch(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(),
            HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
        WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("errors", ex.getBindingResult().getAllErrors());

        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }
}
