package com.reservation.service;

import com.reservation.entity.User;
import com.reservation.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String username,
                             String password,
                             String confirmPassword) {

        username = username.trim();

        if (username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Username and password are required."
            );
        }

        if (!username.matches("^[a-zA-Z0-9_]{3,50}$")) {
            throw new IllegalArgumentException(
                    "Username must be 3-50 characters and contain only letters, numbers or underscores."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Passwords do not match."
            );
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists. Please choose another."
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Never accept the role from the registration form.
        user.setRole("USER");

        userRepository.save(user);
    }
}
