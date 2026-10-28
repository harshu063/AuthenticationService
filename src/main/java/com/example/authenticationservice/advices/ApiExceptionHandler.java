package com.example.authenticationservice.advices;

import com.example.authenticationservice.exceptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://example.invalid/problems/account-already-exists"));
        problem.setTitle("Account already exists");
        problem.setProperty("code", "AUTH_ACCOUNT_EXISTS");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setType(URI.create("https://example.invalid/problems/validation"));
        problem.setTitle("Invalid request");
        problem.setProperty("code", "REQUEST_VALIDATION_FAILED");
        problem.setProperty("errors", errors);
        return problem;
    }
}
