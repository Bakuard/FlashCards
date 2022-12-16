package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.model.auth.policy.PermissionDeniedException;
import com.bakuard.flashcards.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class.getName());


    private final DtoMapper mapper;

    @Autowired
    public ExceptionResolver(DtoMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(value = UnknownEntityException.class)
    public ResponseEntity<ExceptionResponse> handle(UnknownEntityException exception) {
        logger.error("Unknown entity", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessageKey());

        return ResponseEntity.
                status(HttpStatus.NOT_FOUND).
                body(response);
    }

    @ExceptionHandler(value = IncorrectCredentials.class)
    public ResponseEntity<ExceptionResponse> handle(IncorrectCredentials exception) {
        logger.error("Incorrect credentials", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessageKey());

        return ResponseEntity.
                status(HttpStatus.FORBIDDEN).
                body(response);
    }

    @ExceptionHandler(value = InvalidParameter.class)
    public ResponseEntity<ExceptionResponse> handle(InvalidParameter exception) {
        logger.error("Invalid parameter", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessageKey());

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(response);
    }

    @ExceptionHandler(value = InvalidParameter.class)
    public ResponseEntity<ExceptionResponse> handle(DataStoreConstraintViolationException exception) {
        logger.error("Data store constraint violation", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessageKey());

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(response);
    }

    @ExceptionHandler(value = FailToSendMailException.class)
    public ResponseEntity<ExceptionResponse> handle(FailToSendMailException exception) {
        logger.error("Fail to send mail exception", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessageKey());

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(response);
    }

    @ExceptionHandler(value = PermissionDeniedException.class)
    public ResponseEntity<ExceptionResponse> handle(PermissionDeniedException exception) {
        logger.error("Permission denied", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.FORBIDDEN,
                "permissionDenied");

        return ResponseEntity.
                status(HttpStatus.FORBIDDEN).
                body(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handle(ConstraintViolationException exception) {
        logger.error("Incorrect business data", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exception);

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(response);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handle(RuntimeException exception) {
        logger.error("Unexpected exception", exception);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internalServerError");

        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(response);
    }

}
