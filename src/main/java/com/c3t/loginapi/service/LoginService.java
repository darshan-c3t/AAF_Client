package com.c3t.loginapi.service;

import com.c3t.loginapi.dto.LoginDto;
import com.c3t.loginapi.dto.TokenResponse;
import com.c3t.loginapi.dto.TokenUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements ILoginService{

    @Autowired
    AuthService authService;

    @Override
    public TokenResponse login(LoginDto loginDto) {
        TokenResponse response = null;
        try {
            return authService.loginUser(loginDto.getUsername(), loginDto.getPassword());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getApi() {
        return null;
    }

    @Override
    public void writeApi() {

    }
}
