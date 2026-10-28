package com.example.authenticationservice.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.authenticationservice.dtos.SignUpRequestDto;
import com.example.authenticationservice.dtos.SignUpResponseDto;
import com.example.authenticationservice.models.User;
import com.example.authenticationservice.services.AuthService;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrations")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
        User user = authService.signUp(request.getEmail(), request.getPassword());
        SignUpResponseDto response = new SignUpResponseDto();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
