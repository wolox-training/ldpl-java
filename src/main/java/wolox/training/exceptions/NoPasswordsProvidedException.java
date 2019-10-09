package wolox.training.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Old password, new password and confirmation must be provided")
public class NoPasswordsProvidedException extends RuntimeException {

}
