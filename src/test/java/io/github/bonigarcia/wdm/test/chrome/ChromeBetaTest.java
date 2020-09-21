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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
public class ChromeBetaTest {

    static String chromeBetaPath = "/usr/bin/google-chrome-beta";
    static File chromeBetaFile = new File(chromeBetaPath);

    WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        assumeTrue(chromeBetaFile.exists());
        WebDriverManager.chromedriver().clearResolutionCache()
                .browserPath(chromeBetaPath).setup();
    }

    @Before
    public void setupTest() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(chromeBetaPath);
        driver = new ChromeDriver(chromeOptions);
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @After
    public static void teardownClass() {
        WebDriverManager.chromedriver().clearResolutionCache();
    }

    @Test
    public void test() {
        driver.get("https://bonigarcia.github.io/selenium-jupiter/");
        assertThat(driver.getTitle(),
                containsString("JUnit 5 extension for Selenium"));
    }

}
