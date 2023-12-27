/*
 * (C) Copyright 2019 Boni Garcia (https://bonigarcia.github.io/)
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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test asserting Edge driver versions.
 *
 * @author Boni Garcia
 * @since 3.5.0
 */
class EdgeReadVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void edgeVersionsTest() {
        String[] expectedVersions = { "75.0.139.20", "76.0.183.0", "77.0.237.0",
                "78.0.277.0", "79.0.313.0", "80.0.361.111" };
        List<String> driverVersions = WebDriverManager.edgedriver()
                .getDriverVersions();

        log.debug("Expected edge versions: {}",
                Arrays.asList(expectedVersions));
        log.debug("Edge versions read from the repository: {}", driverVersions);

        assertThat(driverVersions).contains(expectedVersions);
    }

}
