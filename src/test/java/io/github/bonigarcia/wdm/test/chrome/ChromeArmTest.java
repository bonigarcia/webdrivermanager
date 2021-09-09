/*
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.chrome;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with ARM64 and Chrome.
 *
 * @author Boni Garcia
 * @since 5.0.2
 */
class ChromeArmTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void testEdgeArm() {
        WebDriverManager wdm = WebDriverManager.chromedriver().mac()
                .avoidBrowserDetection();

        // 1. Force downloading
        wdm.forceDownload();
        checkArm(wdm);

        // 2. Using cache
        wdm.reset();
        checkArm(wdm);
    }

    private void checkArm(WebDriverManager wdm) {
        wdm.setup();
        String driverPath = wdm.getDownloadedDriverPath();
        log.debug("Driver path (X64) {}", driverPath);

        wdm.arm64().setup();
        String driverPathArm64 = wdm.getDownloadedDriverPath();
        log.debug("Driver path (ARM64) {}", driverPathArm64);

        assertThat(driverPath).isNotEqualTo(driverPathArm64);
    }

}
