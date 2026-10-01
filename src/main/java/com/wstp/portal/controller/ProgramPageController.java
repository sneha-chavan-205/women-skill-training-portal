package com.wstp.portal.controller;

import com.wstp.portal.entity.Program;
import com.wstp.portal.service.ProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/programs")
public class ProgramPageController {

    private static final List<String> CATEGORIES = List.of(
            "Digital Skills",
            "Technology",
            "Fashion & Design",
            "Beauty & Wellness",
            "Business",
            "Finance",
            "Communication"
    );

    private final ProgramService programService;

    public ProgramPageController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public String programsPage(@RequestParam(required = false) String category, Model model) {

        String selected = (category == null
                || category.isBlank()
                || category.equalsIgnoreCase("All"))
                ? null
                : category.trim();

        List<Program> programs = (selected == null)
                ? programService.getActivePrograms()
                : programService.getActiveProgramsByCategory(selected);

        model.addAttribute("programs", programs);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("selectedCategory", selected);
        return "programs";
    }

    @GetMapping("/{id}")
    public String programDetails(@PathVariable Long id, Model model) {
        return programService.getProgramById(id)
                .filter(Program::isActive)
                .map(program -> {
                    model.addAttribute("program", program);
                    return "program-details";
                })
                .orElse("redirect:/programs");
    }
}