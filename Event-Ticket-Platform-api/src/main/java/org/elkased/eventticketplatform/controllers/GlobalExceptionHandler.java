package org.elkased.eventticketplatform.controllers;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.elkased.eventticketplatform.domain.dtos.ErrorDto;
import org.elkased.eventticketplatform.exceptions.EventNotFoundException;
import org.elkased.eventticketplatform.exceptions.EventUpdateException;
import org.elkased.eventticketplatform.exceptions.TicketTypeNotFoundException;
import org.elkased.eventticketplatform.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEventNotFoundException(EventNotFoundException ex) {
        log.error("Caught EventNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Event not found");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketTypeNotFoundException(TicketTypeNotFoundException ex) {
        log.error("Caught TicketTypeNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("TicketType not found");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(EventUpdateException.class)
    public ResponseEntity<ErrorDto> handleEventUpdateException(EventUpdateException ex) {
        log.error("Caught EventUpdateException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Unable to update Event");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("Caught UserNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("User not found");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Caught MethodArgumentNotValidException", ex);
        ErrorDto errorDto = new ErrorDto();
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(field -> field.getField() + " : " + field.getDefaultMessage())
                .orElse("Validation error occurred");
        errorDto.setError(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Caught ConstraintViolationException", ex);
        ErrorDto errorDto = new ErrorDto();
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation ->
                        violation.getPropertyPath() + " " + violation.getMessage()
                ).orElse("Violation Constraint occurred");
        errorDto.setError(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("An unknown error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }
}
