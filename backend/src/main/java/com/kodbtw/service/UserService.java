package com.kodbtw.service;

import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.dto.UserResponse;
import com.kodbtw.entity.User;
import com.kodbtw.exception.EmailAlreadyExistsException;
import com.kodbtw.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName().trim(), normalizedEmail, hashedPassword);
        user.setEmailVerified(false);
        User savedUser = userRepository.save(user);

        boolean emailSent = emailVerificationService.issueAndSendVerification(savedUser);
        String message = emailSent
                ? "Registration successful. Please check your email to verify your account."
                : "Account created, but we could not deliver the verification email. Please use the resend button to try again.";

        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), emailSent, message);
    }
}
