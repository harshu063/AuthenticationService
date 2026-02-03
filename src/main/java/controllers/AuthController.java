package controllers;

import dtos.LoginRequestDto;
import dtos.LoginResponseDto;
import dtos.SignUpRequestDto;
import dtos.SignUpResponseDto;
import services.AuthService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService AuthService;

@PostMapping("/sign_up")
    public SignUpResponseDto signUp(SignUpRequestDto request){
    return null;

}
@PostMapping("/login")
public LoginResponseDto login(LoginRequestDto request){
    return null;
}


}
