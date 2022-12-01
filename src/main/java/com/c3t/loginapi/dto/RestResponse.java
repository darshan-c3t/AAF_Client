package com.c3t.loginapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@JsonDeserialize ( builder = RestResponse.RestResponseBuilder.class )
public class RestResponse {

    public static final boolean RESPONSE_FAILURE = false;

    public static final boolean RESPONSE_SUCCESS = true;

    public static final String SUCCESS_STATUS = "success";

    public static final String FAILURE_STATUS = "failure";

    public static final String API_V_REGEX = ".?/api/v\\d{1,}/.";

    public static final EmptyJsonResponse EMPY_JSON_OBJECT = new EmptyJsonResponse();

    private static final String MSG_OPERATION_SUCCEEDED = "Operation succeeded!";

    private Object data;

    private Boolean success;

    private List<Message> messages;

    @JsonDeserialize ( builder = Message.MessageBuilder.class )
    @Builder
    @Getter
    public static class Message {

        private String exception;

        private String message;
        private String status;

        private String requestedUri;

        private String timestamp;

        private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern( "yyyy-MM-dd HH:mm:ss" ).toFormatter();

        @JsonPOJOBuilder ( withPrefix = "" )
        public static class MessageBuilder {

        }

        public static Message build( final Class exceptionClass, final String message ) {
            return Message.builder().exception(exceptionClass.getName()).message( message ).status( FAILURE_STATUS ).requestedUri( extractRequestedUri() ).timestamp( LocalDateTime.now().format( DATE_TIME_FORMATTER ) ).build();

        }

        public static Message build( final String message, final String status ) {
            return Message.builder().message( message ).status( status ).requestedUri( extractRequestedUri() ).timestamp( LocalDateTime.now().format( DATE_TIME_FORMATTER ) ).build();

        }

        public static Message build(Class exceptionClass, String message, String status, String requestURI) {
            return Message.builder().exception(exceptionClass.getName()).message(message).status(status).requestedUri(requestURI).timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER)).build();
        }

        private static String extractRequestedUri() {

            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if ( requestAttributes instanceof ServletRequestAttributes ) {
                HttpServletRequest request = ( (ServletRequestAttributes) requestAttributes ).getRequest();
                return request.getRequestURI();
            }

            return StringUtils.EMPTY;
        }
    }

    @JsonPOJOBuilder ( withPrefix = "" )
    public static class RestResponseBuilder {

    }

    public static RestResponse failureBuild( final Class exceptionClass, final String message ) {
        return RestResponse.builder().data( EMPY_JSON_OBJECT ).success( RESPONSE_FAILURE ).messages( Arrays.asList( Message.build( exceptionClass, message ) ) ).build();
    }

    public static RestResponse failureBuild( final List<Message> messages ) {
        return RestResponse.builder().data( EMPY_JSON_OBJECT ).success( RESPONSE_FAILURE ).messages( messages ).build();
    }

    public static RestResponse build( final Object data, final boolean success, final List<Message> messages ) {
        return RestResponse.builder().data( data ).success( success ).messages( messages ).build();
    }

    @JsonDeserialize
    @JsonSerialize
    public static class EmptyJsonResponse {}

}
