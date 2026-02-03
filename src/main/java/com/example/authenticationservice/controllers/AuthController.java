package com.example.authenticationservice.controllers;

import com.example.authenticationservice.dtos.*;
import com.example.authenticationservice.exceptions.UserNotFoundException;
import com.example.authenticationservice.exceptions.WrongPasswordException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.authenticationservice.services.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


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

}
