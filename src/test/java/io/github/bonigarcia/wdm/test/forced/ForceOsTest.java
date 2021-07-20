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
package io.github.bonigarcia.wdm.test.forced;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
class ForceOsTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriverManager wdm = WebDriverManager.chromedriver()
            .avoidResolutionCache();

    @ParameterizedTest
    @EnumSource(names = { "WIN", "LINUX", "MAC" })
    void testForceOs(OperatingSystem operatingSystem) {
        switch (operatingSystem) {
        case WIN:
            wdm.win().setup();
            break;
        case LINUX:
            wdm.linux().setup();
            break;
        case MAC:
            wdm.mac().setup();
            break;
        }
        File driver = new File(wdm.getDownloadedDriverPath());
        log.debug("OS {} - driver path {}", operatingSystem, driver);
        assertThat(driver).exists();
    }

}