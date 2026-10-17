package com.wstp.portal.selenium;

import com.wstp.portal.entity.Program;
import com.wstp.portal.entity.Role;
import com.wstp.portal.entity.User;
import com.wstp.portal.repository.EnrollmentRepository;
import com.wstp.portal.repository.ProgramRepository;
import com.wstp.portal.repository.UserRepository;
import com.wstp.portal.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:wstp_selenium;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=never"
})
class PortalBrowserTests {

    private static final String TEST_PASSWORD = "BrowserTestPassword21";
    private static final String TEST_EMAIL = "selenium.learner@example.test";
    private static final String TEST_PROGRAM_TITLE = "CI Browser Testing Program";

    @LocalServerPort
    private int port;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserService userService;

    private WebDriver driver;
    private WebDriverWait wait;
    private Long programId;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        userRepository.deleteAll();
        programRepository.deleteAll();

        Program program = programRepository.save(new Program(
                TEST_PROGRAM_TITLE,
                "A deterministic program used by browser CI tests.",
                "Beginner",
                "2 weeks",
                "Technology"));
        programId = program.getId();

        userService.registerUser(new User(
                "Selenium Learner", TEST_EMAIL, TEST_PASSWORD, Role.LEARNER));

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--window-size=1440,1200",
                "--no-first-run",
                "--no-default-browser-check");
        driver = new ChromeDriver(options); // Selenium Manager resolves and caches the matching driver.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void listsTheAvailableProgram() throws Throwable {
        withFailureScreenshot(() -> {
            driver.get(baseUrl() + "/programs");
            assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h3[normalize-space()='" + TEST_PROGRAM_TITLE + "']"))).isDisplayed());
        });
    }

    @Test
    void opensProgramDetails() throws Throwable {
        withFailureScreenshot(() -> {
            driver.get(baseUrl() + "/programs");
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//h3[normalize-space()='" + TEST_PROGRAM_TITLE + "']/ancestor::article//a"))).click();
            wait.until(ExpectedConditions.urlContains("/programs/" + programId));
            assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enrollBtn"))).isDisplayed());
        });
    }

    @Test
    void registersANewLearner() throws Throwable {
        withFailureScreenshot(() -> {
            driver.get(baseUrl() + "/register");
            driver.findElement(By.id("name")).sendKeys("Selenium Registration Learner");
            driver.findElement(By.id("email")).sendKeys("selenium.registration@example.test");
            driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
            driver.findElement(By.id("confirm")).sendKeys(TEST_PASSWORD);
            driver.findElement(By.id("submitBtn")).click();
            assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("continueBtn"))).isDisplayed());
            assertTrue(userRepository.existsByEmail("selenium.registration@example.test"));
        });
    }

    @Test
    void signsInAndRedirectsHome() throws Throwable {
        withFailureScreenshot(() -> {
            signIn();
            wait.until(ExpectedConditions.urlToBe(baseUrl() + "/"));
            assertTrue(driver.findElement(By.tagName("body")).getText().contains("Women Skill Training Portal"));
        });
    }

    @Test
    void enrollsTheSignedInLearner() throws Throwable {
        withFailureScreenshot(() -> {
            signIn();
            wait.until(ExpectedConditions.urlToBe(baseUrl() + "/"));
            driver.get(baseUrl() + "/programs/" + programId);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("enrollBtn"))).click();
            WebElement notice = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enrollNotice")));
            wait.until(ExpectedConditions.attributeContains(notice, "class", "success"));
            assertTrue(notice.getText().contains("successfully enrolled"));
            assertEquals(1, enrollmentRepository.count());
        });
    }

    private void signIn() {
        driver.get(baseUrl() + "/login");
        driver.findElement(By.id("email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("submitBtn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successBox")));
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private void withFailureScreenshot(Executable test) throws Throwable {
        try {
            test.execute();
        } catch (Throwable failure) {
            if (driver instanceof TakesScreenshot screenshots) {
                try {
                    Path directory = Path.of("target", "selenium-screenshots");
                    Files.createDirectories(directory);
                    String name = failure.getClass().getSimpleName() + "-" + System.nanoTime() + ".png";
                    Files.copy(screenshots.getScreenshotAs(OutputType.FILE).toPath(), directory.resolve(name));
                } catch (IOException | RuntimeException screenshotFailure) {
                    failure.addSuppressed(screenshotFailure);
                }
            }
            throw failure;
        }
    }
}
