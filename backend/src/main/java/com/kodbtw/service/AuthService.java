package com.kodbtw.service;

import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.LoginResponse;
import com.kodbtw.dto.UserResponse;
import com.kodbtw.entity.User;
import com.kodbtw.exception.InvalidCredentialsException;
import com.kodbtw.repository.UserRepository;
import com.kodbtw.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail());

        return new LoginResponse(token, userResponse);
    }
}
