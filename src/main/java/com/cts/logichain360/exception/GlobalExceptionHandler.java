package com.cts.logichain360.exception;

import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	//if user already exists in db
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleDuplicate(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    //insufficient stock when placing an order
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",409),
        		HttpStatus.CONFLICT
        );
    }

    //Handling invalid inputs
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>>  handleBadCredentials(BadCredentialsException ex) {
    	return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",401),
        		HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>>  handleNotFound(ResourceNotFoundException ex) {
    	return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",404),
        		HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>  handleBadRequest(IllegalArgumentException ex) {
    	return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",400),
        		HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",400),
        		HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>  handleGeneral(Exception ex) {
    	return new ResponseEntity<Map<String, Object>>(
        		Map.of("Message",ex.getMessage(),
        				"status",500),
        		HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}