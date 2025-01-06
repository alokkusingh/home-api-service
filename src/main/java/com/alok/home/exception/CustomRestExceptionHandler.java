package com.alok.home.exception;

import com.alok.home.commons.dto.exception.GlobalRestExceptionHandler;
import org.decampo.xirr.OverflowException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class CustomRestExceptionHandler extends GlobalRestExceptionHandler {

    @ExceptionHandler(OverflowException.class)
    ProblemDetail handleException(OverflowException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problemDetail.setTitle("Server Error");
        problemDetail.setType(URI.create("home-api/errors/server-error"));
        problemDetail.setProperty("errorCategory", "ServerError");
        problemDetail.setProperty("timestamp", ZonedDateTime.now());
        e.printStackTrace();
        return problemDetail;
    }
}
