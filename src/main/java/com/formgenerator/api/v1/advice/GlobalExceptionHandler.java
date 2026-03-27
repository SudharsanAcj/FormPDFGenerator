package com.formgenerator.api.v1.advice;

import com.formgenerator.domain.dto.response.ErrorResponse;
import com.formgenerator.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .errorCode("VALIDATION_FAILED")
                .message("Request validation failed")
                .validationErrors(errors)
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(InvalidInvestorDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidData(InvalidInvestorDataException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .errorCode("INVALID_INVESTOR_DATA")
                .message(ex.getMessage())
                .validationErrors(ex.getValidationErrors())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(AmcNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAmcNotFound(AmcNotFoundException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .errorCode("AMC_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(AmcMappingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMappingNotFound(AmcMappingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse.builder()
                .errorCode("AMC_MAPPING_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(PdfTemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePdfNotFound(PdfTemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse.builder()
                .errorCode("PDF_TEMPLATE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(PdfFormFillingException.class)
    public ResponseEntity<ErrorResponse> handlePdfFilling(PdfFormFillingException ex) {
        log.error("PDF form filling error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
                .errorCode("PDF_FILLING_ERROR")
                .message("Failed to fill the PDF form. Please contact support.")
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(ESignException.class)
    public ResponseEntity<ErrorResponse> handleESign(ESignException ex) {
        log.error("eSign error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(RtiSubmissionException.class)
    public ResponseEntity<ErrorResponse> handleRtiSubmission(RtiSubmissionException ex) {
        log.error("RTI submission error for AMC [{}]: {}", ex.getAmcCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.builder()
                .errorCode("RTI_SUBMISSION_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .errorCode("TRANSACTION_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please contact support.")
                .timestamp(Instant.now())
                .build());
    }
}
