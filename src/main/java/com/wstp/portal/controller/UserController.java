package com.wstp.portal.controller;

import com.wstp.portal.entity.Role;
import com.wstp.portal.entity.User;
import com.wstp.portal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        if (userService.existsByEmail(request.email())) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        Role role = request.role() == null ? Role.LEARNER : request.role();
        if (role != Role.LEARNER && role != Role.TRAINER) {
            return ResponseEntity.badRequest()
                    .body("Public registration only allows LEARNER or TRAINER accounts");
        }

        User user = new User(request.name(), request.email(), request.password(), role);
        User savedUser = userService.registerUser(user);

        return ResponseEntity.ok(new UserResponse(savedUser));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {

        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
