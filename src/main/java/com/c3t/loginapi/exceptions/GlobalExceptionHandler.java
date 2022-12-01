package com.c3t.loginapi.exceptions;

import com.c3t.loginapi.dto.RestResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String AUTHENTICATION_FAILED = "Authentication Failed";
    private static final Logger LOGGER = LogManager.getLogger( GlobalExceptionHandler.class );

    /**
     * This method overrides the model returned by Spring handled exceptions.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {

        return new ResponseEntity<>(buildErrorResponse((Throwable) ex,
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()), status);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleDataNotFoundException(final BadRequestException exception,
                                                              final HttpServletRequest request) {
        RestResponse.Message failureMessage = RestResponse.Message.build(exception.getClass(),
                getErrorMsgWithRootCause(exception));

        RestResponse dataNotFoundResponse = RestResponse.build(RestResponse.EMPY_JSON_OBJECT,
                RestResponse.RESPONSE_FAILURE, Arrays.asList(failureMessage));

        return new ResponseEntity<>(dataNotFoundResponse, HttpStatus.OK);
    }



    @ExceptionHandler({ RuntimeException.class, NullPointerException.class })
    public ResponseEntity<Object> handleRuntimeException(final RuntimeException exception,
                                                         final HttpServletRequest request) {
        LOGGER.error("Exception: ", exception);
        return new ResponseEntity<>(buildErrorResponse(exception, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(final RuntimeException exception, final HttpServletRequest request) {
        LOGGER.error("Access Denied Exception: ", exception);
        return new ResponseEntity<>(buildErrorResponse(exception, request), HttpStatus.FORBIDDEN);
    }


    private RestResponse buildErrorResponse(Throwable throwable, HttpServletRequest request) {

        String errorMsgWithRootCause = getErrorMsgWithRootCause(throwable);

        List<RestResponse.Message> messages = (List<RestResponse.Message>) request.getAttribute("messages");

        if (Objects.nonNull(messages)) {
            messages.add(RestResponse.Message.build(throwable.getClass(), errorMsgWithRootCause));
            return RestResponse.failureBuild(messages);
        }

        return RestResponse.failureBuild(throwable.getClass(), errorMsgWithRootCause);
    }

    private String getErrorMsgWithRootCause(Throwable throwable) {
        Throwable rootException = throwable;
        while (rootException.getCause() != null && rootException.getCause() != rootException) {
            rootException = rootException.getCause();
        }

        final StringBuilder errorMessageBuilder = new StringBuilder();
        appendIfNotNull(throwable, errorMessageBuilder);

        if (ObjectUtils.notEqual(rootException, throwable)) {
            errorMessageBuilder.append(". Root cause: ");
            appendIfNotNull(rootException, errorMessageBuilder);

        }
        return errorMessageBuilder.toString();
    }

    private void appendIfNotNull(Throwable throwable, final StringBuilder errorMessageBuilder) {
        if (Objects.nonNull(throwable) && StringUtils.isNotEmpty(throwable.getMessage())) {
            errorMessageBuilder.append(throwable.getMessage());
        }
    }

}