package com.karan.inventory_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Product ka inventory record nahi mila
    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleInventoryNotFound(
            InventoryNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problem.setTitle("Inventory not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    // Product hai, lekin available stock kam hai
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStock(
            InsufficientStockException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Insufficient stock");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    // Service ke input checks fail hue
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleInvalidArgument(
            IllegalArgumentException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problem.setTitle("Invalid input");

        return ResponseEntity.badRequest().body(problem);
    }

    // Request DTO ke validation annotations fail hue
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid"
        );
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }
}