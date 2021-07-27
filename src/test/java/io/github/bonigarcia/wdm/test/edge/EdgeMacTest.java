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
package io.github.bonigarcia.wdm.test.edge;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test msedgedriver in Mac OS.
 *
 * @author Boni Garcia
 * @since 4.3.1
 */
class EdgeMacTest {

    static final Logger log = getLogger(lookup().lookupClass());

    @Test
    void testEdgeMac() {
        String libName = "libc++.dylib";
        String driverVersion = "87.0.664.75";

        WebDriverManager wdm = WebDriverManager.edgedriver()
                .driverVersion(driverVersion).mac().avoidResolutionCache()
                .avoidBrowserDetection();
        wdm.setup();
        String downloadedDriverPath = wdm.getDownloadedDriverPath();

        log.debug("The downloaded driver path is: {}", downloadedDriverPath);
        File driver = new File(downloadedDriverPath);
        assertThat(driver).exists();

        File lib = new File(driver.getParent(), libName);
        assertThat(lib).exists();
    }

}
