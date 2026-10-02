package com.wstp.portal.controller;

import com.wstp.portal.entity.Enrollment;
import com.wstp.portal.service.EnrollmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // =========================================================
    // ENROLL USER IN A PROGRAM
    // =========================================================

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


    // =========================================================
    // GET ALL PROGRAMS ENROLLED BY CURRENT USER
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyEnrollments(
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity.status(401)
                        .body("Please login first");
            }

            String email = authentication.getName();

            List<Enrollment> enrollments =
                    enrollmentService.getUserEnrollments(email);

            return ResponseEntity.ok(enrollments);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // CHECK WHETHER USER IS ALREADY ENROLLED
    // =========================================================

    @GetMapping("/check/{programId}")
    public ResponseEntity<?> checkEnrollment(
            @PathVariable Long programId,
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity.status(401)
                        .body(false);
            }

            String email = authentication.getName();

            boolean enrolled =
                    enrollmentService.isUserEnrolled(
                            email,
                            programId
                    );

            return ResponseEntity.ok(enrolled);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // GET TOTAL ENROLLMENT COUNT
    // =========================================================

    @GetMapping("/count")
    public ResponseEntity<?> getEnrollmentCount(
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity.status(401)
                        .body(0);
            }

            String email = authentication.getName();

            long count =
                    enrollmentService.getEnrollmentCount(email);

            return ResponseEntity.ok(count);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // GET COMPLETED PROGRAM COUNT
    // =========================================================

    @GetMapping("/completed-count")
    public ResponseEntity<?> getCompletedCount(
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity.status(401)
                        .body(0);
            }

            String email = authentication.getName();

            long count =
                    enrollmentService.getCompletedCount(email);

            return ResponseEntity.ok(count);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}