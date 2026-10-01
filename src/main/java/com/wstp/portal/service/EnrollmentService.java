package com.wstp.portal.service;

import com.wstp.portal.entity.Enrollment;
import com.wstp.portal.entity.EnrollmentStatus;
import com.wstp.portal.entity.Program;
import com.wstp.portal.entity.User;
import com.wstp.portal.repository.EnrollmentRepository;
import com.wstp.portal.repository.ProgramRepository;
import com.wstp.portal.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            ProgramRepository programRepository,
            UserRepository userRepository) {

        this.enrollmentRepository = enrollmentRepository;
        this.programRepository = programRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Enrollment enrollUser(String email, Long programId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() ->
                        new RuntimeException("Program not found"));

        if (!program.isActive()) {
            throw new RuntimeException("This program is not active");
        }

        if (enrollmentRepository.existsByUserAndProgram(user, program)) {
            throw new RuntimeException(
                    "You are already enrolled in this program");
        }

        Enrollment enrollment = new Enrollment();

        enrollment.setUser(user);
        enrollment.setProgram(program);
        enrollment.setProgress(0);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getUserEnrollments(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return enrollmentRepository
                .findByUserOrderByEnrolledAtDesc(user);
    }

    public boolean isUserEnrolled(String email, Long programId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() ->
                        new RuntimeException("Program not found"));

        return enrollmentRepository.existsByUserAndProgram(user, program);
    }

    public long getEnrollmentCount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return enrollmentRepository.countByUser(user);
    }

    public long getCompletedCount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return enrollmentRepository.countByUserAndStatus(
                user,
                EnrollmentStatus.COMPLETED
        );
    }
}