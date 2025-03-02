/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.chromium;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.openqa.selenium.net.PortProber.findFreePort;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Google Chromium browser.
 *
 * @author Boni Garcia
 * @since 3.8.0
 */
class ChromiumTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver;

    static Optional<Path> browserPath;

    @BeforeAll
    static void setupClass() {
        browserPath = WebDriverManager.chromiumdriver().getBrowserPath();
        assumeThat(browserPath).isPresent();

        WebDriverManager.chromiumdriver().setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary(browserPath.get().toFile());
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-debugging-port=" + findFreePort());
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void teardown() {
        driver.quit();
    }

    @Test
    void test() {
        String sutUrl = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(sutUrl);
        String title = driver.getTitle();
        log.debug("The title of {} is {}", sutUrl, title);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Wait<WebDriver> wait = new WebDriverWait(driver,
                Duration.ofSeconds(30));
        wait.until(d -> d.getTitle().contains("Selenium WebDriver"));
    }

}
