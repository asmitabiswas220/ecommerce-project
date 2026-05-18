package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        user.setUsername(normalizeUsername(user.getUsername()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User save(User user) {
        user.setUsername(normalizeUsername(user.getUsername()));
        return userRepository.save(user);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
    }

    public User login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordMatches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(normalizeUsername(username));
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || rawPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
