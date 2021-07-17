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
package io.github.bonigarcia.wdm.test.generic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with generic manager.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
class GenericCreateTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver;

    WebDriverManager wdm;

    @AfterEach
    void teardown() {
        wdm.quit();
    }

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "chrome-in-docker" })
    void test(String defaultBrowser) {
        System.setProperty("wdm.defaultBrowser", defaultBrowser);
        wdm = WebDriverManager.getInstance();
        driver = wdm.create();

        String sutUrl = "https://github.com/bonigarcia/webdrivermanager";
        driver.get(sutUrl);
        String title = driver.getTitle();
        log.debug("The title of {} is {}", sutUrl, title);

        assertThat(title)
                .contains("Automated driver management for Selenium WebDriver");
    }

}
