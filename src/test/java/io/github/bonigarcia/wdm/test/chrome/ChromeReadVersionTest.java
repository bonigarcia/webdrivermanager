/*
 * (C) Copyright 2024 Boni Garcia (https://bonigarcia.github.io/)
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

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test asserting Chrome driver versions.
 *
 * @author Boni Garcia
 * @since 5.6.4
 */
class ChromeReadVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void chromeVersionsTest() {
        List<String> driverVersions = WebDriverManager.chromedriver()
                .getDriverVersions();
        log.debug("Chrome versions read from the repository: {}",
                driverVersions);

        driverVersions.forEach(
                version -> assertThat(isValidVersion(version)).isTrue());
    }

    boolean isValidVersion(String version) {
        return Pattern.matches("^\\d+(\\.\\d+)*$", version);
    }

}
