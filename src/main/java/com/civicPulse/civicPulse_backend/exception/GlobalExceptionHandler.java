package com.civicPulse.civicPulse_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================================
    // 404 — resource nahi mila
    // ==========================================================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {

        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, ex, false);
    }



    // ==========================================================
    // 400 — business rule violation
    // ==========================================================
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException ex, HttpServletRequest req) {

        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, ex, false);
    }

    // ==========================================================
    // 400 — @Valid fail (field-wise errors)
    // ==========================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        // LinkedHashMap — field order preserve hota hai, HashMap me random aata hai
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> fieldErrors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(g -> fieldErrors.putIfAbsent(g.getObjectName(), g.getDefaultMessage()));

        String traceId = newTraceId();
        log.warn("[{}] Validation failed on {}: {}", traceId, req.getRequestURI(), fieldErrors);

        return ResponseEntity.badRequest().body(
                ApiError.validation(400, "Kuch fields galat hain",
                        req.getRequestURI(), traceId, fieldErrors));
    }

    // ==========================================================
    // 400 — malformed JSON ya galat enum value body me
    // ==========================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        String msg = "Request body padha nahi ja saka. JSON aur enum values check karein.";

        // Jackson enum error se valid values nikaal ke user ko bata do
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
                && ife.getTargetType() != null && ife.getTargetType().isEnum()) {

            msg = "'" + ife.getValue() + "' valid value nahi hai. Allowed: "
                    + String.join(", ", enumNames(ife.getTargetType()));
        }

        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", msg, req, ex, false);
    }

    // ==========================================================
    // 400 — URL me galat type/enum (@PathVariable Department)
    // ==========================================================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        String msg;
        Class<?> type = ex.getRequiredType();
        if (type != null && type.isEnum()) {
            msg = "'" + ex.getValue() + "' valid " + type.getSimpleName()
                    + " nahi hai. Allowed: " + String.join(", ", enumNames(type));
        } else {
            msg = "Parameter '" + ex.getName() + "' ki value ka format galat hai";
        }

        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", msg, req, ex, false);
    }

    // ==========================================================
    // 400 — required query param missing
    // ==========================================================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {

        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Required parameter missing: " + ex.getParameterName(), req, ex, false);
    }

    // ==========================================================
    // 401 — galat email/password
    // Message hamesha same rakho: "email exist karta hai ya nahi" ye leak na ho
    // ==========================================================
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {

        log.warn("Failed login attempt from IP {}", req.getRemoteAddr());

        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Email ya password galat hai", req, ex, false);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(
            AuthenticationException ex, HttpServletRequest req) {

        return build(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                "Authentication required", req, ex, false);
    }

    // ==========================================================
    // 403 — YE SABSE ZAROORI THA
    // Ye handler na ho to AccessDeniedException catch-all me chala jata hai
    // aur citizen ko /api/admin/** par 403 ki jagah 500 milta hai
    // ==========================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        log.warn("Access denied: {} {} ", req.getMethod(), req.getRequestURI());

        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Is action ki permission nahi hai", req, ex, false);
    }

    // ==========================================================
    // 405 — galat HTTP method
    // ==========================================================
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {

        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                ex.getMethod() + " is endpoint par allowed nahi hai", req, ex, false);
    }

    // ==========================================================
    // 404 — unknown route
    // (spring.mvc.throw-exception-if-no-handler-found=true chahiye)
    // ==========================================================
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(
            NoHandlerFoundException ex, HttpServletRequest req) {

        return build(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "Aisa koi endpoint nahi hai", req, ex, false);
    }

    // ==========================================================
    // 409 — DB constraint (duplicate email, FK violation)
    // getMessage() kabhi client ko na bhejo: table/constraint names leak hote hain
    // ==========================================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest req) {

        String raw = ex.getMostSpecificCause().getMessage();
        String safe = "Ye record pehle se exist karta hai ya related data missing hai";

        if (raw != null) {
            String lower = raw.toLowerCase();
            if (lower.contains("email"))       safe = "Ye email already registered hai";
            else if (lower.contains("phone"))  safe = "Ye phone number already registered hai";
            else if (lower.contains("ticket")) safe = "Ticket number clash ho gaya, dobara try karein";
        }

        // raw message sirf logs me
        return build(HttpStatus.CONFLICT, "CONFLICT", safe, req, ex, true);
    }

    // ==========================================================
    // 413 — photo upload size limit (complaint photos ke liye)
    // ==========================================================
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest req) {

        return build(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "Photo bahut badi hai. Max 5 MB allowed hai.", req, ex, false);
    }
    // ==========================================================
    // 500 — catch-all
    // Client ko SIRF generic message + traceId. Detail logs me.
    // ==========================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest req) {

        String traceId = newTraceId();

        log.error("[{}] Unhandled {} on {} {}", traceId,
                ex.getClass().getSimpleName(), req.getMethod(), req.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiError.of(500, "INTERNAL_ERROR",
                        "Kuch galat ho gaya. Support ko ye ID batayein: " + traceId,
                        req.getRequestURI(), traceId));
    }

    // ==========================================================
    // Helpers  <-- YE MISSING THE
    // ==========================================================
    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                           HttpServletRequest req, Exception ex,
                                           boolean logStackTrace) {

        String traceId = newTraceId();

        if (logStackTrace) {
            log.error("[{}] {} on {} {}", traceId, code, req.getMethod(), req.getRequestURI(), ex);
        } else {
            log.debug("[{}] {} on {} {}: {}", traceId, code,
                    req.getMethod(), req.getRequestURI(), ex.getMessage());
        }

        return ResponseEntity.status(status).body(
                ApiError.of(status.value(), code, message, req.getRequestURI(), traceId));
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String[] enumNames(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = ((Enum<?>) constants[i]).name();
        }
        return names;
    }
}

// ======