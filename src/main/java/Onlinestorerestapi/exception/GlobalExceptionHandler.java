package Onlinestorerestapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // handles spring.servlet.multipart.max-file-size and spring.servlet.multipart.max-request-size violation exceptions
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeException(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("One or more file exceed the maximum allowed size limit");
    }

    // missing RequestPart
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        String errorMessage = String.format("Required part '%s' is missing", ex.getRequestPartName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorMessage);
    }

    // Spring Boot fails to parse or map json
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("invalid json");
    }

    // Spring Boot fails to parse or map query parameters or path variables
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        String errorMessage = String.format(
                "Parameter '%s' must be of type '%s'. Value '%s' is invalid.",
                parameterName, expectedType, invalidValue
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorMessage);
    }

    // Spring Boot fails to validate @RequestBody fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(error.getField(), key -> new java.util.ArrayList<>()).add(error.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    // Spring Boot fails to validate @RequestParam, @PathVariable or direct method parameters
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleMethodValidationException(HandlerMethodValidationException exception) {
        Map<String, List<String>> errors = new HashMap<>();

        exception.getParameterValidationResults().forEach(result -> {
            String fieldName = Optional.ofNullable(result.getMethodParameter().getParameterName())
                    .orElse("unknown");

            result.getResolvableErrors().forEach(error -> {
                String message = error.getDefaultMessage();
                errors.computeIfAbsent(fieldName, key -> new ArrayList<>()).add(message);
            });
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    // when the request body has an unsupported format
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        Map<String, String> errors = Map.of(
                "error", "Unsupported media type",
                "supported media types", ex.getSupportedMediaTypes().toString());
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errors);
    }

    // handle ApiException
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(exception.getErrors());
    }

    // returned value could not be serialized into what the user needs
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Acceptable");
        error.put("message", "Supported media type is application/json");
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    // returned value could be serialized into what the user needs, but failed
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Acceptable");
        error.put("message", "Supported media type is application/json");
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

}
