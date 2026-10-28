package com.example.authenticationservice;

import com.example.authenticationservice.exceptions.UserAlreadyExistsException;
import com.example.authenticationservice.models.Role;
import com.example.authenticationservice.models.User;
import com.example.authenticationservice.repositories.RoleRepository;
import com.example.authenticationservice.repositories.UserRepository;
import com.example.authenticationservice.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTests {
    private UserRepository users;
    private RoleRepository roles;
    private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        roles = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new AuthService(users, roles, passwordEncoder);
    }

    @Test
    void signUpNormalizesEmailHashesPasswordAndAssignsUserRole() throws Exception {
        Role role = new Role();
        role.setRole("USER");
        when(users.findByEmail("person@example.com")).thenReturn(Optional.empty());
        when(roles.findByRole("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("a-long-password")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.signUp(" Person@Example.COM ", "a-long-password");

        assertEquals("person@example.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
        assertEquals(1, user.getRoles().size());
        verify(users).save(user);
    }

    @Test
    void signUpRejectsExistingNormalizedEmail() {
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> service.signUp("PERSON@example.com", "a-long-password"));
    }
}
