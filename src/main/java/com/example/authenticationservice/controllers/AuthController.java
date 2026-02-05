package com.example.authenticationservice.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.authenticationservice.dtos.LoginRequestDto;
import com.example.authenticationservice.dtos.LoginResponseDto;
import com.example.authenticationservice.dtos.RequestStatus;
import com.example.authenticationservice.dtos.SignUpRequestDto;
import com.example.authenticationservice.dtos.SignUpResponseDto;
import com.example.authenticationservice.services.AuthService;



@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService AuthService;
    public AuthController(AuthService authService) {
        this.AuthService = authService;
    }

@PostMapping("/sign_up")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody SignUpRequestDto request) {
    SignUpResponseDto response = new SignUpResponseDto();
        try {
            if (AuthService.signUp(request.getEmail(),request.getPassword())){
                response.setRequestStatus(RequestStatus.SUCCESS);
            }
            else{
                response.setRequestStatus(RequestStatus.FAILURE);
            }
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            response.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }



}
@PostMapping("/login")
public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request){
        try {
            String token = AuthService.login(request.getEmail(),request.getPassword());
            LoginResponseDto LoginDto = new LoginResponseDto();
            LoginDto.setRequestStatus(RequestStatus.SUCCESS);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Auth token", token);
            ResponseEntity<LoginResponseDto> response = new ResponseEntity<>(
                    LoginDto, headers, HttpStatus.OK
            );
            return response;

        }catch (Exception e){
            LoginResponseDto LoginDto = new LoginResponseDto();
            LoginDto.setRequestStatus(RequestStatus.FAILURE);
            ResponseEntity<LoginResponseDto> response = new ResponseEntity<>(LoginDto, HttpStatus.BAD_REQUEST);
            return response;
        }
}
        @GetMapping("/validate")
        public boolean validate(@RequestParam("token") String token){
        return AuthService.validate(token);
}

        @DeleteMapping
    public void logout(@RequestHeader("Authorization") String token){
        AuthService.logout(token);
    }

}
