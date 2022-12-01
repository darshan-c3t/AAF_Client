package com.c3t.loginapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUserResponse {
    private Long clientId;
    private Long userId;
    private Map<String, String > customPropeties;

}
