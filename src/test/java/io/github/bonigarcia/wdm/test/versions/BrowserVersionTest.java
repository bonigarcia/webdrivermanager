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
package io.github.bonigarcia.wdm.test.versions;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using major versions of browsers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.0.0
 */
public class BrowserVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @BeforeAll
    @AfterAll
    public static void cleanCache() {
        WebDriverManager.chromedriver().clearResolutionCache();
    }

    @Test
    public void testChromeVersion() {
        WebDriverManager driverManager = WebDriverManager.chromedriver();
        driverManager.browserVersion("81").setup();
        assertDriver(driverManager);
    }

    @Test
    public void testFirefoxVersion() {
        WebDriverManager driverManager = WebDriverManager.firefoxdriver();
        driverManager.browserVersion("75").setup();
        assertDriver(driverManager);
    }

    private void assertDriver(WebDriverManager driverManager) {
        File driver = new File(driverManager.getDownloadedDriverPath());
        log.debug("Driver path {}", driver);
        assertThat(driver.exists());
    }

}
