package com.c3t.loginapi.service;

import com.c3t.loginapi.dto.*;
import com.c3t.loginapi.exceptions.UnAuthorizedExceptions;
import com.c3t.loginapi.security.CustomAuthenticationProvider;
import com.c3t.loginapi.security.UserDetailsSecurityContext;
import com.c3t.loginapi.utils.ApplicationConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AuthService {

    private ObjectMapper mapper = new ObjectMapper();

    @Value("${client.username}")
    private String clientName;

    @Value("${client.password}")
    private String clientPassword;

    @Value("${auth.tool.url}")
    private String authUrl;

    private final String VERIFY_URL = "/verify";
    private final String LOGIN_URL = "/login";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;

    public TokenUserResponse verifyToken (String token) throws Exception {
        TokenVerifyRequest tokenVerifyRequest= TokenVerifyRequest.builder().token(token).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, createBaseAuth());
        HttpEntity<TokenVerifyRequest> request = new HttpEntity<>(tokenVerifyRequest, headers);
        ResponseEntity<RestResponse> response = restTemplate.exchange(authUrl + VERIFY_URL, HttpMethod.POST, request, RestResponse.class);
        if(!StringUtils.equalsAnyIgnoreCase(response.getStatusCode().toString(), HttpStatus.OK.toString())) {
            throw new UnAuthorizedExceptions("Unauthorized, can not access server");
        }
        if(!response.getBody().getSuccess()) {
            throw new UnAuthorizedExceptions("Invalid token provided");
        }
        return mapper.convertValue(response.getBody().getData(), new TypeReference<TokenUserResponse>() { });

    }

    public TokenResponse loginUser (String userName, String password) throws Exception {
        LoginDto loginObj = LoginDto.builder().username(userName).password(password).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, createBaseAuth());

        HttpEntity<LoginDto> request = new HttpEntity<>(loginObj, headers);

        ResponseEntity<RestResponse> response = restTemplate.exchange(authUrl + LOGIN_URL, HttpMethod.POST, request, RestResponse.class);
        if(!StringUtils.equalsAnyIgnoreCase(response.getStatusCode().toString(), HttpStatus.OK.toString())) {
            throw new UnAuthorizedExceptions("Unauthorized, can not access server");
        }
        if(!response.getBody().getSuccess()) {
            throw new UnAuthorizedExceptions("Invalidn username and password");
        }
        return mapper.convertValue(response.getBody().getData(), new TypeReference<TokenResponse>() { });
        //return (TokenResponse) response.getBody().getData();
    }

    public String createBaseAuth () {
        String plainCreds = clientName + ":" + clientPassword;
        return "basic " + Base64.getEncoder().encodeToString(plainCreds.getBytes());
    }

    public String getValueFromHeader(HttpServletRequest httpRequest, String key) {
        Stream var10000 = Collections.list(httpRequest.getHeaderNames()).stream();
        Function var10001 = (h) -> {
            return h;
        };
        httpRequest.getClass();
        Map<String, String> headers = (Map)var10000.collect(Collectors.toMap(var10001, httpRequest::getHeader));
        return (String)headers.get(key);
    }

    public void setAuthenticationDetails(HttpServletRequest request, String xSsoToken, UserDetailsSecurityContext userDetailsSecurityContext) {
        Set<SimpleGrantedAuthority> authoritySet = new HashSet<>();
        Set<String> primaryOrgIdsSet = new HashSet<>();
        Set<String> orgRolesSet = new HashSet<>();
        for ( String role : userDetailsSecurityContext.getRoles() ) {
            orgRolesSet.add(role);
            authoritySet.add(new SimpleGrantedAuthority(ApplicationConstants.ROLE_AUTH + role));
        }
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>(authoritySet);
        List<String> primaryOrgIds = new ArrayList<>(primaryOrgIdsSet);
        List<String> orgRoles = new ArrayList<>(orgRolesSet);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( xSsoToken, userDetailsSecurityContext, authorityList );
        Authentication authentication = customAuthenticationProvider.authenticate( authRequest );

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication( authentication );
        HttpSession httpSession = request.getSession( Boolean.TRUE );
        httpSession.setAttribute( HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext );
    }

    public boolean validateToken (HttpServletRequest request, String token ) {
        try {
            TokenUserResponse response = verifyToken(token);
            UserDetailsSecurityContext userDetailsSecurityContext = new UserDetailsSecurityContext();
            userDetailsSecurityContext.setName(response.getUserId().toString());
            if (Objects.nonNull(response.getCustomPropeties())) {
                Map<String, String> properties = response.getCustomPropeties();
                if (properties.containsKey("role")) {
                    String roles = properties.get("role").toUpperCase(Locale.ROOT);
                    System.out.println(roles);
                    if (StringUtils.isNotBlank(roles)) {
                        userDetailsSecurityContext.setRoles(Arrays.asList(roles.split(",")));
                    }
                }
            }
            setAuthenticationDetails(request, token, userDetailsSecurityContext);
            return true;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }
}
