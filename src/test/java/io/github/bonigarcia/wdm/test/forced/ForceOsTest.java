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

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.online.Downloader;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
class ForceOsTest {

    final Logger log = getLogger(lookup().lookupClass());

    @InjectMocks
    Downloader downloader;

    @Spy
    Config config = new Config();

    @BeforeEach
    void setup() {
        openMocks(this);
        WebDriverManager.chromedriver().clearDriverCache();
    }

    @AfterEach
    void teardown() {
        WebDriverManager.chromedriver().clearDriverCache();
    }

    @ParameterizedTest
    @EnumSource(names = { "WIN", "LINUX", "MAC" })
    void testForceOs(OperatingSystem operatingSystem) {
        switch (operatingSystem) {
        case WIN:
            chromedriver().win().avoidResolutionCache().setup();
            break;
        case LINUX:
            chromedriver().linux().avoidResolutionCache().setup();
            break;
        case MAC:
            chromedriver().mac().avoidResolutionCache().setup();
            break;
        }
        File driver = new File(chromedriver().getDownloadedDriverPath());
        log.debug("OS {} - driver path {}", operatingSystem, driver);
        assertThat(driver.exists());
    }

}