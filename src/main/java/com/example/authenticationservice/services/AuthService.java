package com.example.authenticationservice.services;

import com.example.authenticationservice.exceptions.UserAlreadyExistsException;
import com.example.authenticationservice.models.Role;
import com.example.authenticationservice.models.User;
import com.example.authenticationservice.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import com.example.authenticationservice.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signUp(String email, String password) throws UserAlreadyExistsException {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("An account already exists for that email address");
        }

        Role defaultRole = roleRepository.findByRole("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRole("USER");
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.getRoles().add(defaultRole);
        return userRepository.save(user);
    }
}
