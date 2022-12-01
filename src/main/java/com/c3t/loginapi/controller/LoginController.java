package com.c3t.loginapi.controller;

import com.c3t.loginapi.dto.LoginDto;
import com.c3t.loginapi.dto.RestResponse;
import com.c3t.loginapi.dto.TokenResponse;
import com.c3t.loginapi.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value ="/client")
public class LoginController {

    @Autowired
    LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public TokenResponse login (@RequestBody LoginDto loginDto) throws Exception {
        return loginService.login(loginDto);
    }

    @RequestMapping(value = "/basic", method = RequestMethod.GET)
    public RestResponse basicAuthApi () throws Exception {
        return RestResponse.builder().data("Its basic authencated api").success(RestResponse.RESPONSE_SUCCESS).build();
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @RequestMapping(value = "/writeSuperApi", method = RequestMethod.POST)
    public void writeSuperApi (@RequestBody LoginDto loginDto) throws Exception {
        loginService.writeApi();
    }


}
