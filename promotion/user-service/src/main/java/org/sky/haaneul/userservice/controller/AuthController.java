package org.sky.haaneul.userservice.controller;

import io.jsonwebtoken.Claims;
import org.sky.haaneul.userservice.dto.UserDto;
import org.sky.haaneul.userservice.entity.User;
import org.sky.haaneul.userservice.service.JWTService;
import org.sky.haaneul.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {
    private final JWTService jwtService;
    private final UserService userService;

    public AuthController(JWTService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto.LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(UserDto.LoginResponse.builder()
                .token(token)
                .user(UserDto.Response.from(user))
                .build());


    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody UserDto.TokenRequest request) {
        Claims claims = jwtService.validateToken(request.getToken());

        return ResponseEntity.ok(UserDto.TokenResponse.builder()
                .email(claims.getSubject())
                .valid(true)
                .role(claims.get("role", String.class))
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody UserDto.TokenRequest tokenRequest) {
        String newToken = jwtService.refreshToken(tokenRequest.getToken());
        return ResponseEntity.ok(Collections.singletonMap("token", newToken));
    }


}
