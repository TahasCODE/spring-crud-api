package com.example.CRUD.controller;

import com.example.CRUD.DTO.*;
import com.example.CRUD.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
    }

    @PostMapping("/register/customer")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register/employee")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse registerEmployee(@Valid @RequestBody RegisterEmployeeRequest request) {
        return authService.registerEmployee(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // protected: shows who the token belongs to
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new CurrentUserResponse(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role"));
    }
}