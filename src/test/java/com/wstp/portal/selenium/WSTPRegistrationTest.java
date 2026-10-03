package com.wstp.portal.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WSTPRegistrationTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {

        driver = new ChromeDriver();

        wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(20)
        );

        driver.manage().window().maximize();

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(2)
        );
    }

    @Test
    void testSuccessfulRegistration() {

        try {

            // --------------------------------------------------
            // 1. OPEN REGISTRATION PAGE
            // --------------------------------------------------

            driver.get(BASE_URL + "/register");

            wait.until(
                    ExpectedConditions.urlContains("/register")
            );

            // Wait until registration page is actually loaded
            wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.tagName("body")
                    )
            );

            // --------------------------------------------------
            // 2. FULL NAME
            // --------------------------------------------------

            WebElement nameField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(
                                    "input[id='name'], " +
                                    "input[name='name'], " +
                                    "input[placeholder*='name' i]"
                            )
                    )
            );

            nameField.clear();

            nameField.sendKeys(
                    "Selenium Test User"
            );

            // --------------------------------------------------
            // 3. EMAIL
            // --------------------------------------------------

            WebElement emailField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(
                                    "input[type='email'], " +
                                    "input[id='email'], " +
                                    "input[name='email']"
                            )
                    )
            );

            String uniqueEmail =
                    "selenium" +
                    System.currentTimeMillis() +
                    "@example.com";

            emailField.clear();

            emailField.sendKeys(
                    uniqueEmail
            );

            // --------------------------------------------------
            // 4. PASSWORD
            // --------------------------------------------------

            List<WebElement> passwordFields =
                    wait.until(
                            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                    By.cssSelector(
                                            "input[type='password']"
                                    )
                            )
                    );

            if (passwordFields.size() < 2) {

                throw new RuntimeException(
                        "Registration page does not contain two password fields. " +
                        "Found: " + passwordFields.size()
                );
            }

            WebElement passwordField =
                    passwordFields.get(0);

            WebElement confirmPasswordField =
                    passwordFields.get(1);

            passwordField.clear();

            passwordField.sendKeys(
                    "Test@12345"
            );

            // --------------------------------------------------
            // 5. CONFIRM PASSWORD
            // --------------------------------------------------

            confirmPasswordField.clear();

            confirmPasswordField.sendKeys(
                    "Test@12345"
            );

            // --------------------------------------------------
            // 6. ACCOUNT TYPE
            // --------------------------------------------------
            // Learner is normally selected by default.
            // If a radio button exists, select learner.

            List<WebElement> learnerOptions =
                    driver.findElements(
                            By.cssSelector(
                                    "input[value='LEARNER'], " +
                                    "input[value='learner'], " +
                                    "input[id*='learner' i]"
                            )
                    );

            if (!learnerOptions.isEmpty()) {

                WebElement learner =
                        learnerOptions.get(0);

                if (!learner.isSelected()) {
                    learner.click();
                }
            }

            // --------------------------------------------------
            // 7. FIND CREATE ACCOUNT BUTTON
            // --------------------------------------------------

            WebElement registerButton =
                    wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath(
                                            "//button[" +
                                            "contains(" +
                                            "translate(normalize-space(.)," +
                                            "'ABCDEFGHIJKLMNOPQRSTUVWXYZ'," +
                                            "'abcdefghijklmnopqrstuvwxyz')," +
                                            "'create account')" +
                                            "]"
                                    )
                            )
                    );

            // Scroll button into view
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            registerButton
                    );

            // Small explicit wait instead of Thread.sleep
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            registerButton
                    )
            );

            // --------------------------------------------------
            // 8. SUBMIT
            // --------------------------------------------------

            registerButton.click();

            // --------------------------------------------------
            // 9. WAIT FOR REGISTRATION RESULT
            // --------------------------------------------------

            wait.until(driver -> {

                String currentUrl =
                        driver.getCurrentUrl();

                String pageText =
                        driver.findElement(
                                By.tagName("body")
                        )
                        .getText()
                        .toLowerCase();

                return
                        !currentUrl.contains("/register")
                        ||
                        pageText.contains("success")
                        ||
                        pageText.contains("dashboard")
                        ||
                        pageText.contains("login")
                        ||
                        pageText.contains("account created")
                        ||
                        pageText.contains("welcome");
            });

            // --------------------------------------------------
            // 10. VALIDATE RESULT
            // --------------------------------------------------

            String currentUrl =
                    driver.getCurrentUrl();

            String pageText =
                    driver.findElement(
                            By.tagName("body")
                    )
                    .getText()
                    .toLowerCase();

            boolean registrationSuccessful =
                    !currentUrl.contains("/register")
                    ||
                    pageText.contains("success")
                    ||
                    pageText.contains("dashboard")
                    ||
                    pageText.contains("login")
                    ||
                    pageText.contains("account created")
                    ||
                    pageText.contains("welcome");

            assertTrue(
                    registrationSuccessful,
                    "Registration was not successful. " +
                    "Current URL: " + currentUrl +
                    " | Page text: " + pageText
            );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "REGISTRATION TEST PASSED"
            );

            System.out.println(
                    "Email used: " + uniqueEmail
            );

            System.out.println(
                    "Final URL: " + currentUrl
            );

            System.out.println(
                    "========================================"
            );

        } catch (Exception e) {

            takeScreenshot(
                    "registration-failure"
            );

            throw e;
        }
    }

    // ----------------------------------------------------------
    // SCREENSHOT ON FAILURE
    // ----------------------------------------------------------

    private void takeScreenshot(String fileName) {

        try {

            File screenshot =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(
                                    OutputType.FILE
                            );

            Path target =
                    Path.of(
                            "target",
                            "selenium-screenshots",
                            fileName + ".png"
                    );

            Files.createDirectories(
                    target.getParent()
            );

            Files.copy(
                    screenshot.toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println(
                    "Screenshot saved to: "
                    + target.toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not save screenshot: "
                    + e.getMessage()
            );
        }
    }

    // ----------------------------------------------------------
    // CLOSE BROWSER
    // ----------------------------------------------------------

    @AfterEach
    void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}