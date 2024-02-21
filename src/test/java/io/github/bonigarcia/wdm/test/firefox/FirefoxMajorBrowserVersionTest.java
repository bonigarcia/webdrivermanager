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
package io.github.bonigarcia.wdm.test.firefox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bonigarcia.wdm.WebDriverManager;

class FirefoxMajorBrowserVersionTest {

    @ParameterizedTest
    @MethodSource("data")
    void test(String browserVersion, String driverVersion) {
        WebDriverManager wdm = WebDriverManager.firefoxdriver()
                .browserVersion(browserVersion).avoidResolutionCache();
        wdm.setup();
        assertThat(wdm.getDownloadedDriverVersion()).isEqualTo(driverVersion);
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("101", "0.31.0"),
                Arguments.of("91", "0.31.0"), Arguments.of("90", "0.30.0"),
                Arguments.of("62", "0.29.1"), Arguments.of("53", "0.18.0"));
    }

}
