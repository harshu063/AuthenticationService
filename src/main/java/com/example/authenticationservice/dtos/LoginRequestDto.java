package com.example.authenticationservice.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
//    private String token;
//    private String type;
//    private String username;
    private String email;
    private String password;
//    private String role;
}
