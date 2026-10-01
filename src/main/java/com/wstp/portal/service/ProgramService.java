package com.wstp.portal.service;

import com.wstp.portal.entity.Program;
import com.wstp.portal.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public List<Program> getActivePrograms() {
        return programRepository.findByActiveTrue();
    }

    public List<Program> getActiveProgramsByCategory(String category) {
        return programRepository.findByActiveTrueAndCategory(category);
    }

    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

    public Optional<Program> getProgramById(Long id) {
        return programRepository.findById(id);
    }

    public Program saveProgram(Program program) {
        return programRepository.save(program);
    }
}