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
package io.github.bonigarcia.wdm.test.firefox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.OperatingSystem;

class Firefox90Test {

    @ParameterizedTest
    @MethodSource("data")
    void test(OperatingSystem os, String driverVersion) {
        WebDriverManager wdm = WebDriverManager.firefoxdriver()
                .operatingSystem(os).browserVersion("90").avoidResolutionCache()
                .useLocalVersionsPropertiesFirst();
        wdm.setup();
        assertThat(wdm.getDownloadedDriverVersion()).isEqualTo(driverVersion);
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of(OperatingSystem.MAC, "0.29.0"),
                Arguments.of(OperatingSystem.LINUX, "0.29.1"),
                Arguments.of(OperatingSystem.WIN, "0.29.1"));
    }

}
