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
package io.github.bonigarcia.wdm.test.versions;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.versions.VersionDetector;

/**
 * Commands database test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.4.0
 */
class BrowserVersionDetectionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void commandsTest() throws Exception {
        Config config = new Config();
        HttpClient httpClient = new HttpClient(config);
        VersionDetector versionDetector = new VersionDetector(config,
                httpClient);
        String[] browsers = { "chrome", "firefox", "edge", "opera",
                "chromium" };
        for (String browser : browsers) {
            Optional<String> detectedVersion = versionDetector
                    .getBrowserVersionFromTheShell(browser);
            if (detectedVersion.isPresent()) {
                log.debug("The detected version of {} is {}", browser,
                        detectedVersion.get());
                int numericVersion = Integer.parseInt(detectedVersion.get());
                assertThat(numericVersion).isPositive();
            }
        }

    }

}
