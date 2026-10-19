package com.kodbtw.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GoogleAuthConfigurationException.class)
    public ResponseEntity<Map<String, String>> handleGoogleAuthConfiguration(GoogleAuthConfigurationException ex) {
        return googleError(HttpStatus.SERVICE_UNAVAILABLE, "GOOGLE_AUTH_NOT_CONFIGURED",
                "Google sign-in is not configured. Contact the application administrator.");
    }

    @ExceptionHandler(GoogleIdentityVerificationException.class)
    public ResponseEntity<Map<String, String>> handleGoogleVerificationUnavailable(GoogleIdentityVerificationException ex) {
        return googleError(HttpStatus.SERVICE_UNAVAILABLE, "GOOGLE_VERIFICATION_UNAVAILABLE",
                "Google sign-in could not be verified right now. Please try again.");
    }

    @ExceptionHandler(InvalidGoogleCredentialException.class)
    public ResponseEntity<Map<String, String>> handleInvalidGoogleCredential(InvalidGoogleCredentialException ex) {
        return googleError(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_CREDENTIAL",
                "Google sign-in could not be verified. Please try again.");
    }

    @ExceptionHandler(GoogleAccountExistsException.class)
    public ResponseEntity<Map<String, String>> handleExistingGoogleAccount(GoogleAccountExistsException ex) {
        return googleError(HttpStatus.CONFLICT, "GOOGLE_ACCOUNT_REQUIRES_EXISTING_SIGN_IN",
                "An account already exists for this email. Sign in using your existing method; Google linking is not available yet.");
    }

    @ExceptionHandler(GoogleIdentityConflictException.class)
    public ResponseEntity<Map<String, String>> handleGoogleIdentityConflict(GoogleIdentityConflictException ex) {
        return googleError(HttpStatus.CONFLICT, "GOOGLE_IDENTITY_CONFLICT",
                "This Google sign-in conflicted with another account update. Please retry.");
    }

    private ResponseEntity<Map<String, String>> googleError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("error", "Google Authentication Error", "code", code, "message", message));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Unauthorized",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflict",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DuplicatePlatformException.class)
    public ResponseEntity<Map<String, String>> handleDuplicatePlatform(
            DuplicatePlatformException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflict",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflict",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnsupportedPlatformException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedPlatform(
            UnsupportedPlatformException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PlatformApiException.class)
    public ResponseEntity<Map<String, String>> handlePlatformApi(
            PlatformApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Bad Gateway",
                "message", "The platform is temporarily unavailable. Please try again later."
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("message", "Input validation failed");
        response.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(
            Exception ex) {
        Throwable rootCause = ex;
        for (int depth = 0; depth < 8 && rootCause.getCause() != null
                && rootCause.getCause() != rootCause; depth++) {
            rootCause = rootCause.getCause();
        }
        StackTraceElement origin = ex.getStackTrace().length == 0 ? null : ex.getStackTrace()[0];
        log.error("Unhandled exception type={} rootCauseType={} origin={}",
                ex.getClass().getName(), rootCause.getClass().getName(), origin);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred. Please try again later."
        ));
    }
}
