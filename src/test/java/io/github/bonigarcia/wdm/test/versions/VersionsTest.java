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
package io.github.bonigarcia.wdm.test.versions;

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static io.github.bonigarcia.wdm.WebDriverManager.iedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.operadriver;
import static io.github.bonigarcia.wdm.WebDriverManager.phantomjs;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test getting all versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
class VersionsTest {

    final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @MethodSource("data")
    void testChromeDriverVersions(WebDriverManager driverManager) {
        List<String> versions = driverManager.getDriverVersions();
        log.debug("Versions of {} {}", driverManager.getClass().getSimpleName(),
                versions);
        assertThat(versions).isNotNull().isNotEmpty();
    }

    static Stream<WebDriverManager> data() {
        return Stream.of(chromedriver(), firefoxdriver(), operadriver(),
                edgedriver(), iedriver(), phantomjs());
    }

}
