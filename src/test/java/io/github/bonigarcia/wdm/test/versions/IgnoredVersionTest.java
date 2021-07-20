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

    @Test
    void ignoredVersionsChrome() {
        String driverVersion = "81.0.4044.69";
        String[] ignoredVersions = { driverVersion };

        WebDriverManager wdm = WebDriverManager.chromedriver().forceDownload()
                .driverVersion(driverVersion)
                .ignoreDriverVersions(ignoredVersions).avoidFallback();
        assertThatThrownBy(wdm::setup)
                .isInstanceOf(WebDriverManagerException.class);
    }

    @Test
    void ignoredVersionsFirefox() {
        String[] ignoredVersions = { "0.27.0", "0.26.0" };
        WebDriverManager wdm = WebDriverManager.firefoxdriver();
        wdm.ignoreDriverVersions(ignoredVersions).setup();
        String driverVersion = wdm.getDownloadedDriverVersion();
        log.debug("Resolved version {}", driverVersion);
        assertThat(ignoredVersions).doesNotContain(driverVersion);
    }

}
