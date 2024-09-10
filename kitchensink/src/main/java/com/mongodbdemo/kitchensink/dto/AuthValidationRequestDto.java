package com.mongodbdemo.kitchensink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor public class AuthValidationRequestDto {
    private String accessToken;
    private List<String> roles;
}