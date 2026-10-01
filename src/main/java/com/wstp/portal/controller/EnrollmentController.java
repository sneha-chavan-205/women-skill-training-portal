package com.wstp.portal.controller;

import com.wstp.portal.entity.Enrollment;
import com.wstp.portal.service.EnrollmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{programId}")
    public ResponseEntity<?> enroll(
            @PathVariable Long programId,
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity.status(401)
                        .body("Please login first");
            }

            String email = authentication.getName();

            Enrollment enrollment =
                    enrollmentService.enrollUser(email, programId);

            return ResponseEntity.ok(enrollment);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}