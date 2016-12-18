package org.vyhlidka.homeautomation.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.vyhlidka.homeautomation.repo.ElementNotFoundExcepion;

/**
 * Created by lucky on 18.12.16.
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = ElementNotFoundExcepion.class)
    public String handleBaseException(ElementNotFoundExcepion e){
        return e.getMessage();
    }

}
