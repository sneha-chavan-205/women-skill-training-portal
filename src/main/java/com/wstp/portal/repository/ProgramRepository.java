package com.wstp.portal.repository;

import com.wstp.portal.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findByActiveTrue();

    List<Program> findByActiveTrueAndCategory(String category);
}