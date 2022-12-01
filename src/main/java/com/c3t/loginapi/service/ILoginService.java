package com.c3t.loginapi.service;

import com.c3t.loginapi.dto.LoginDto;
import com.c3t.loginapi.dto.TokenResponse;

public interface ILoginService {
    TokenResponse login(LoginDto loginDto);
    String getApi();
    void writeApi();
}
