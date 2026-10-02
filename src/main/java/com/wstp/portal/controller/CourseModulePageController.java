package com.wstp.portal.controller;

import com.wstp.portal.entity.CourseModule;
import com.wstp.portal.entity.Enrollment;
import com.wstp.portal.entity.Program;
import com.wstp.portal.service.CourseModuleService;
import com.wstp.portal.service.EnrollmentService;
import com.wstp.portal.service.ProgramService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/learning")
public class CourseModulePageController {

    private final CourseModuleService courseModuleService;
    private final ProgramService programService;
    private final EnrollmentService enrollmentService;

    public CourseModulePageController(
            CourseModuleService courseModuleService,
            ProgramService programService,
            EnrollmentService enrollmentService) {

        this.courseModuleService = courseModuleService;
        this.programService = programService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{programId}")
    public String learningPage(
            @PathVariable Long programId,
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        String email = authentication.getName();

        if (!enrollmentService.isUserEnrolled(email, programId)) {
            return "redirect:/programs/" + programId;
        }

        Program program = programService.getProgramById(programId)
                .filter(Program::isActive)
                .orElse(null);

        if (program == null) {
            return "redirect:/programs";
        }

        List<CourseModule> modules =
                courseModuleService.getActiveModules(programId);

        model.addAttribute("program", program);
        model.addAttribute("modules", modules);

        return "course-modules";
    }
}