package com.sispro3d.unam.api.exception;

import com.sispro3d.unam.api.error.ErrorDetail;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Central error handling for the REST API under {@code /api/v1}.
 * Returns a consistent {@link ErrorDetail} body plus the proper HTTP status.
 */
@RestControllerAdvice(basePackages = "com.sispro3d.unam.api")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleNotFound(ResourceNotFoundException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Recurso no encontrado: uri={}, mensaje={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleEntityNotFound(EntityNotFoundException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Entidad no encontrada: uri={}, mensaje={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handleValidation(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorDetail> handleForbidden(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Acceso denegado: uri={}, mensaje={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({BadRequestException.class, InvalidRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorDetail> handleBadRequest(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Solicitud inválida: uri={}, mensaje={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetail> handleUnreadable(HttpMessageNotReadableException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Cuerpo de solicitud malformado: uri={}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Tipo de parámetro inválido: uri={}, param={}", request.getRequestURI(), ex.getName());
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " + ex.getName(), request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetail> handleConstraintViolation(ConstraintViolationException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Violación de restricción: uri={}", request.getRequestURI());
        List<String> details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorDetail> handleConflict(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Conflicto: uri={}, mensaje={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleGeneric(Exception ex, jakarta.servlet.http.HttpServletRequest request) {
        log.error("Error inesperado: uri={}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request, List.of());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorDetail> build(HttpStatus status, String message,
                                              jakarta.servlet.http.HttpServletRequest request, List<String> details) {
        ErrorDetail body = ErrorDetail.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details);
        return new ResponseEntity<>(body, status);
    }
}
