package com.solutiondeliveryroaster.solutiondelivery.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateTeamNameException.class)
    public ErrorResponse handleDuplicateTeamException(DuplicateTeamNameException exception){
        logger.error("DuplicateTeamException occurred: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateTeamMemberException.class)
    public ErrorResponse handleDuplicateTeamMemberException(DuplicateTeamMemberException exception){
        logger.error("DuplicateTeamMemberException occurred: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException exception){
        logger.error("ResourceNotFoundException occurred: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}
