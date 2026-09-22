package com.moveinsync.shuttle.controller;

import com.moveinsync.shuttle.entity.AppUser;
import com.moveinsync.shuttle.exception.ApiException;
import com.moveinsync.shuttle.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public record RegisterRequest(@Email @NotBlank String email, @Size(min = 8) String password) {
    }

    public record RegisterResponse(Long userId, String email, String status) {
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        if (users.findByEmail(request.email()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(encoder.encode(request.password()));
        AppUser saved = users.save(user);

        return new RegisterResponse(saved.getId(), saved.getEmail(), "REGISTERED");
    }
}
