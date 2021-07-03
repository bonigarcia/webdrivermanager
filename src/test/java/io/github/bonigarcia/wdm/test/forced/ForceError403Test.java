/*
 * (C) Copyright 2019 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/**
 * Force download test.
 *
 * @author Boni Garcia
 * @since 3.3.0
 */
class ForceError403Test {

    final Logger log = getLogger(lookup().lookupClass());

    static final int NUM = 40;

    @Disabled
    @Test
    void test403() {
        for (int i = 0; i < NUM; i++) {
            log.debug("Forcing 403 error {}/{}", i + 1, NUM);
            firefoxdriver().avoidBrowserDetection().avoidResolutionCache()
                    .setup();
            assertThat(firefoxdriver().getDownloadedDriverPath()).isNotNull();
        }
    }

}
