package com.wstp.portal.repository;

import com.wstp.portal.entity.CourseModule;
import com.wstp.portal.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseModuleRepository
        extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByProgramAndActiveTrueOrderByModuleOrderAsc(
            Program program
    );

    List<CourseModule> findByProgramOrderByModuleOrderAsc(
            Program program
    );

    long countByProgram(Program program);
}