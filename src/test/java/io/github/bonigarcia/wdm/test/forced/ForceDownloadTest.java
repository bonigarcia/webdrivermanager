/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Force download test.
 *
 * @author Boni Garcia
 * @since 2.1.1
 */
class ForceDownloadTest {

    @ParameterizedTest
    @ValueSource(classes = { ChromeDriver.class, FirefoxDriver.class,
            OperaDriver.class, EdgeDriver.class })
    void testForceDownload(Class<? extends WebDriver> driverClass) {
        WebDriverManager wdm = WebDriverManager.getInstance(driverClass);
        wdm.forceDownload().avoidTmpFolder().avoidBrowserDetection()
                .avoidReadReleaseFromRepository().timeout(20)
                .operatingSystem(WIN).setup();
        assertThat(wdm.getDownloadedDriverPath()).isNotNull();
    }

}
