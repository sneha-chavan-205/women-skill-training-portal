package com.wstp.portal.service;

import com.wstp.portal.entity.CourseModule;
import com.wstp.portal.entity.Program;
import com.wstp.portal.repository.CourseModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseModuleService {

    private final CourseModuleRepository courseModuleRepository;
    private final ProgramService programService;

    public CourseModuleService(
            CourseModuleRepository courseModuleRepository,
            ProgramService programService) {

        this.courseModuleRepository = courseModuleRepository;
        this.programService = programService;
    }

    public List<CourseModule> getActiveModules(Long programId) {

        Program program = programService.getProgramById(programId)
                .orElseThrow(() ->
                        new RuntimeException("Program not found"));

        return courseModuleRepository
                .findByProgramAndActiveTrueOrderByModuleOrderAsc(program);
    }

    public List<CourseModule> getAllModules(Long programId) {

        Program program = programService.getProgramById(programId)
                .orElseThrow(() ->
                        new RuntimeException("Program not found"));

        return courseModuleRepository
                .findByProgramOrderByModuleOrderAsc(program);
    }

    public Optional<CourseModule> getModuleById(Long id) {
        return courseModuleRepository.findById(id);
    }

    public CourseModule saveModule(CourseModule module) {
        return courseModuleRepository.save(module);
    }

    public long getModuleCount(Long programId) {

        Program program = programService.getProgramById(programId)
                .orElseThrow(() ->
                        new RuntimeException("Program not found"));

        return courseModuleRepository.countByProgram(program);
    }
}