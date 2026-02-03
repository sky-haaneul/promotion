package org.sky.haaneul.userservice.controller;

import org.sky.haaneul.userservice.dto.UserDto;
import org.sky.haaneul.userservice.entity.User;
import org.sky.haaneul.userservice.entity.UserLoginHistory;
import org.sky.haaneul.userservice.exception.DuplicateUserException;
import org.sky.haaneul.userservice.exception.UnauthorizedAccessException;
import org.sky.haaneul.userservice.exception.UserNotFoundException;
import org.sky.haaneul.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDto.SignupRequest request) {
        User user = userService.createUser(request.getEmail(), request.getPassword(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.Response.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestHeader("X-USER-ID") Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserDto.Response.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestHeader("X-USER-ID") Long userId,
                                           @RequestBody UserDto.UpdateRequest request) {
        User user = userService.updateUser(userId, request.getName());
        return ResponseEntity.ok(UserDto.Response.from(user));
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changePassword(@RequestHeader("X-USER-ID") Long userId,
                                            @RequestBody UserDto.PasswordChangeRequest request) {
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/login-history")
    public ResponseEntity<List<UserLoginHistory>> getLoginHistory(@RequestHeader("X-USER-ID") Long userId) {
        List<UserLoginHistory> history = userService.getUserLoginHistory(userId);
        return ResponseEntity.ok(history);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<String> handleDuplicateUser(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }


}
