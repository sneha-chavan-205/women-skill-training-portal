package com.wstp.portal.repository;

import com.wstp.portal.entity.Enrollment;
import com.wstp.portal.entity.User;
import com.wstp.portal.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserAndProgram(User user, Program program);

    List<Enrollment> findByUserOrderByEnrolledAtDesc(User user);

    long countByUser(User user);

    long countByUserAndStatus(
            User user,
            com.wstp.portal.entity.EnrollmentStatus status
    );
}