package wolox.training.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The password you are trying to update does not match with the user password")
public class UserPasswordMismatch extends RuntimeException {

}
