package com.kodbtw.controller;

import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.LoginResponse;
import com.kodbtw.dto.GoogleLoginRequest;
import com.kodbtw.dto.GoogleAuthChallengeResponse;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.dto.UserResponse;
import com.kodbtw.entity.User;
import com.kodbtw.service.AuthService;
import com.kodbtw.service.GoogleAuthService;
import com.kodbtw.service.GoogleAuthChallengeService;
import com.kodbtw.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final GoogleAuthChallengeService googleAuthChallengeService;

    public AuthController(UserService userService, AuthService authService, GoogleAuthService googleAuthService,
                          GoogleAuthChallengeService googleAuthChallengeService) {
        this.userService = userService;
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.googleAuthChallengeService = googleAuthChallengeService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(googleAuthService.authenticate(request));
    }

    @PostMapping("/google/challenge")
    public ResponseEntity<GoogleAuthChallengeResponse> googleChallenge() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(googleAuthChallengeService.issue());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));
    }
}
