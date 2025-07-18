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
package io.github.bonigarcia.wdm.test.versions;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using major versions of browsers.
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
class BrowserVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void testChromeLatestVersion() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        wdm.setup();
        assertBrowserVersion(wdm, Optional.empty());
    }

    @Test
    void testChromeVersion() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        String browserVersion = "91";
        wdm.browserVersion(browserVersion).avoidResolutionCache().setup();
        assertDriver(wdm);
        assertBrowserVersion(wdm, Optional.of(browserVersion));
    }

    @Test
    void testFirefoxVersion() {
        WebDriverManager wdm = WebDriverManager.firefoxdriver();
        String browserVersion = "90";
        wdm.browserVersion(browserVersion).avoidResolutionCache().setup();
        assertDriver(wdm);
        assertBrowserVersion(wdm, Optional.of(browserVersion));
    }

    private void assertDriver(WebDriverManager wdm) {
        File driver = new File(wdm.getDownloadedDriverPath());
        log.debug("Driver path {}", driver);
        assertThat(driver).exists();
    }

    private void assertBrowserVersion(WebDriverManager wdm,
            Optional<String> expectedBrowserVersion) {
        String resolvedBrowserVersion = wdm.getResolvedBrowserVersion();
        log.debug("Resolved browser version {}", resolvedBrowserVersion);
        assertThat(resolvedBrowserVersion).isNotNull();

        if (expectedBrowserVersion.isPresent()) {
            assertThat(expectedBrowserVersion.get())
                    .isEqualTo(resolvedBrowserVersion);
        }
    }

}
