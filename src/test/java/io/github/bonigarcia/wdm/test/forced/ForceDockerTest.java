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
package io.github.bonigarcia.wdm.test.forced;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for unexpected conditions related to Docker.
 * 
 * @since 5.0.0
 */
class ForceDockerTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void testSeleniumServerUrl() {
        URL seleniumServerUrl = WebDriverManager.getInstance()
                .getDockerSeleniumServerUrl();
        assertThat(seleniumServerUrl).isNull();
    }

    @Test
    void testNoVncUrl() {
        URL noVncUrl = WebDriverManager.getInstance().getDockerNoVncUrl();
        assertThat(noVncUrl).isNull();
    }

    @Test
    void testRecordingPath() {
        Path path = WebDriverManager.getInstance().getDockerRecordingPath();
        assertThat(path).isNull();
    }

}