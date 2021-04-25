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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with ARM64 and Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.4.2
 */
public class EdgeArmTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testEdgeArm() {
        WebDriverManager driverManager = WebDriverManager.edgedriver();

        // 1. Force downloading (empty cache)
        driverManager.clearDriverCache();
        checkArm(driverManager);

        // 2. Using cache
        checkArm(driverManager);
    }

    private void checkArm(WebDriverManager driverManager) {
        driverManager.setup();
        String driverPath = driverManager.win().getDownloadedDriverPath();
        log.debug("Driver path (default arch) {}", driverPath);

        driverManager.arm64().setup();
        String driverPathArm64 = driverManager.win().getDownloadedDriverPath();
        log.debug("Driver path (ARM64) {}", driverPathArm64);

        assertThat(driverPath, not(equalTo(driverPathArm64)));
    }

}
