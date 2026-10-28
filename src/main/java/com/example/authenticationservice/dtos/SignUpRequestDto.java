package com.example.authenticationservice.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class SignUpRequestDto {
    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @NotBlank
    @Size(min = 12, max = 128)
    private String password;
}
