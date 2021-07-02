/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm.test.chrome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Google Chrome beta.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.2.1
 */
class ChromeBetaTest {

    static String chromeBetaPath = "/usr/bin/google-chrome-beta";
    static File chromeBetaFile = new File(chromeBetaPath);

    WebDriver driver;

    @BeforeAll
    static void setupClass() {
        assumeThat(chromeBetaFile).exists();
        WebDriverManager.chromedriver().clearResolutionCache()
                .browserVersionDetectionCommand(chromeBetaPath + " --version")
                .setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(chromeBetaPath);
        driver = new ChromeDriver(chromeOptions);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterAll
    static void teardownClass() {
        WebDriverManager.chromedriver().clearResolutionCache();
    }

    @Test
    void test() {
        driver.get("https://bonigarcia.github.io/selenium-jupiter/");
        assertThat(driver.getTitle())
                .contains("JUnit 5 extension for Selenium");
    }

}
