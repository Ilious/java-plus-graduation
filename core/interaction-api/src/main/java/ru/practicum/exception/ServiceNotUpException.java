package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceNotUpException extends RuntimeException {

    public ServiceNotUpException(String operation, Throwable cause) {
        super(operation, cause);
    }
}
