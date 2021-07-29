/*		
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)		
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
class IgnoredVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriverManager wdm;

    @AfterEach
    void teardown() {
        wdm.clearResolutionCache();
    }

    @Test
    void ignoredVersionsChrome() {
        String driverVersion = "91.0.4472.101";
        String[] ignoredVersions = { driverVersion };

        wdm = WebDriverManager.chromedriver().forceDownload()
                .driverVersion(driverVersion)
                .ignoreDriverVersions(ignoredVersions).avoidFallback();
        assertThatThrownBy(wdm::setup)
                .isInstanceOf(WebDriverManagerException.class);
    }

    @Test
    void ignoredVersionsFirefox() {
        String[] ignoredVersions = { "0.28.0", "0.29.0" };
        wdm = WebDriverManager.firefoxdriver()
                .ignoreDriverVersions(ignoredVersions);
        wdm.setup();
        String driverVersion = wdm.getDownloadedDriverVersion();
        log.debug("Resolved version {}", driverVersion);
        assertThat(ignoredVersions).doesNotContain(driverVersion);
    }

}
