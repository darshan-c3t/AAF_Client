package com.c3t.loginapi.security;

import com.c3t.loginapi.dto.RestResponse;
import com.c3t.loginapi.service.AuthService;
import com.c3t.loginapi.utils.ApplicationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class AuthSecurity extends OncePerRequestFilter {

    @Autowired
    AuthService authService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String AUTHENTICATION_FAILED = "Authentication Failed";

    List<String> SKIP_URL = Arrays.asList( "/client/login", "/client/basic" );

    private static PathPatternParser parser = new PathPatternParser();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUrl = request.getRequestURI();
        Boolean isMatched = SKIP_URL.stream().anyMatch(url -> {
            PathPattern pattern = parser.parse(url);
            return pattern.matches(PathContainer.parsePath(requestUrl));
        });
        if(isMatched) {
            filterChain.doFilter(request, response);
        } else {
            String authHeader = authService.getValueFromHeader(request, ApplicationConstants.AUTHORIZATION);
            if(StringUtils.isBlank(authHeader)) {
                ResponseEntity<Object> handleException = handleUnauthorizedException(new Exception(), request);
                writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, handleException.getBody());
                return;
            }
            if(!StringUtils.contains(authHeader, "bearer")) {
                ResponseEntity<Object> handleException = handleUnauthorizedException(new Exception(), request);
                writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, handleException.getBody());
                return;
            }
            authHeader = authHeader.replace("bearer ", "");
            if(!authService.validateToken(request, authHeader)) {
                ResponseEntity<Object> handleException = handleUnauthorizedException(new Exception(), request);
                writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, handleException.getBody());
                return;
            }


            filterChain.doFilter(request, response);
        }

    }

    public ResponseEntity<Object> handleUnauthorizedException(Exception exception, HttpServletRequest request ) {

        RestResponse.Message failureMessage;
        if ( Objects.isNull( exception ) || Objects.isNull( request ) ) {
            failureMessage = RestResponse.Message.build( this.getClass(), AUTHENTICATION_FAILED, RestResponse.FAILURE_STATUS, "" );
        } else {
            failureMessage = RestResponse.Message.build( exception.getClass(), AUTHENTICATION_FAILED, RestResponse.FAILURE_STATUS, request.getRequestURI() );
        }
        RestResponse unAuthorizedResponse = RestResponse.build( RestResponse.EMPY_JSON_OBJECT, RestResponse.RESPONSE_FAILURE, Arrays.asList( failureMessage ) );

        return new ResponseEntity<>( unAuthorizedResponse, HttpStatus.UNAUTHORIZED );
    }

    public void writeResponse( final HttpServletResponse httpResponse, final int httpStatusCode, Object errorDto ) throws IOException {
        httpResponse.setStatus( httpStatusCode );
        httpResponse.setContentType( MediaType.APPLICATION_JSON_VALUE );

        final PrintWriter out = httpResponse.getWriter();
        if ( Objects.nonNull( out ) ) {
            out.print( objectMapper.writeValueAsString( errorDto ) );
            out.flush();
        }
    }
}
