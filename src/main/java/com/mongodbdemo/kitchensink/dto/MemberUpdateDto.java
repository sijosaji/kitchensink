package com.mongodbdemo.kitchensink.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MemberUpdateDto {
    @Nullable
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String name;

    @Nullable
    @Email
    private String email;

    @Nullable
    @Size(min = 10, max = 12)
    @Digits(fraction = 0, integer = 12)
    private String phoneNumber;
}
