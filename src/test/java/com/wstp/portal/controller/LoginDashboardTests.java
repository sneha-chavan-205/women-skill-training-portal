package com.wstp.portal.controller;

import com.wstp.portal.entity.Role;
import com.wstp.portal.entity.User;
import com.wstp.portal.repository.UserRepository;
import com.wstp.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:wstp_login_dashboard;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=never"
})
class LoginDashboardTests {

    private static final String EMAIL = "dashboard.learner@example.test";
    private static final String PASSWORD = "DashboardPassword21";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void createLearner() {
        userRepository.deleteAll();
        userService.registerUser(new User("Dashboard Learner", EMAIL, PASSWORD, Role.LEARNER));
    }

    @Test
    void anonymousDashboardRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void successfulLoginSavesSessionAndAllowsDashboardAccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "dashboard.learner@example.test",
                                  "password": "DashboardPassword21"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session, "Successful login should persist an authenticated session");

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }
}
