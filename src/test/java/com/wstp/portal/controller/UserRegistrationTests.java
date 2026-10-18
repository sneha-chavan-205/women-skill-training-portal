package com.wstp.portal.controller;

import com.wstp.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:wstp_registration;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=never"
})
class UserRegistrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registersLearnerByDefaultWhenRoleIsOmitted() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Default Learner",
                                  "email": "default.learner@example.test",
                                  "password": "RegistrationPassword21"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LEARNER"));

        assertTrue(
                userRepository.existsByEmail("default.learner@example.test"));
    }

    @Test
    void allowsTrainerRegistration() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Trainer",
                                  "email": "trainer.registration@example.test",
                                  "password": "RegistrationPassword21",
                                  "role": "TRAINER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TRAINER"));

        assertTrue(
                userRepository.existsByEmail("trainer.registration@example.test"));
    }

    @Test
    void rejectsAdminRoleSubmittedToPublicRegistration() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forged Admin",
                                  "email": "forged.admin@example.test",
                                  "password": "RegistrationPassword21",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Public registration only allows LEARNER or TRAINER accounts"));

        assertFalse(userRepository.existsByEmail("forged.admin@example.test"));
    }
}
