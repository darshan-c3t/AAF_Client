package com.c3t.loginapi.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsSecurityContext implements Serializable {

    private String name;
    private List<String> roles;
    private List<String> scopes;

    @Builder(builderMethodName = "userDetailsSecurityContext")
    public UserDetailsSecurityContext(String name, List<String> roles, List<String> scopes) {
        this.name = name;
        this.roles = roles;
        this.scopes = scopes;
    }
}
