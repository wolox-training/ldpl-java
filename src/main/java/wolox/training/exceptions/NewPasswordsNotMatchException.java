package wolox.training.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "New password and its confirmation values must be equals")
public class NewPasswordsNotMatchException extends RuntimeException {

}
