/*
 * (C) Copyright 2022 Boni Garcia (https://bonigarcia.github.io/)
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Google Chrome beta in Windows.
 *
 * @author Boni Garcia
 * @since 5.1.0
 */
class ChromeBetaWindowsTest {

    static String chromeBetaPath = "C:\\\\PROGRA~1\\\\GOOGLE\\\\CHROME~1\\\\APPLIC~1\\\\CHROME.EXE";
    static File chromeBetaFile = new File(chromeBetaPath);

    WebDriver driver;

    @BeforeAll
    static void setupClass() {
        assumeThat(chromeBetaFile).exists();
        String chromeBetaCommand = String.format(
                "cmd.exe /C wmic datafile where name=\"%s\" get Version /value",
                chromeBetaPath);
        WebDriverManager.chromedriver()
                .browserVersionDetectionCommand(chromeBetaCommand).setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(chromeBetaPath);
        driver = new ChromeDriver(chromeOptions);
    }

    @AfterEach
    void teardown() {
        driver.quit();
    }

    @Test
    void test() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        assertThat(driver.getTitle()).contains("Selenium WebDriver");
    }

}
