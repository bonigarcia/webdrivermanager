/*
 * (C) Copyright 2021 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.create;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Chrome and WebDriverManager's creator.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
@EnabledOnOs(LINUX)
class ChromeListCreateTest {

    final Logger log = getLogger(lookup().lookupClass());

    List<WebDriver> drivers;

    WebDriverManager wdm = WebDriverManager.chromedriver();

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        drivers = wdm.capabilities(options).create(2);
    }

    @AfterEach
    void teardown() {
        wdm.quit();
    }

    @Test
    void test() {
        drivers.forEach((driver) -> {
            String sutUrl = "https://github.com/bonigarcia/webdrivermanager";
            driver.get(sutUrl);
            assertThat(driver).isNotNull();
        });
    }

}
