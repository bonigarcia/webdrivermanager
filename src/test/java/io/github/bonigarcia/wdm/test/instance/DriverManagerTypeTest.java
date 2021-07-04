/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.instance;

import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.config.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.config.DriverManagerType.SAFARI;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Test the class name values from DriverManagerTypeTest
 *
 * @author Elias Nogueira and Boni Garcia
 * @since 3.8.1
 */
class DriverManagerTypeTest {

    @ParameterizedTest
    @MethodSource("data")
    void shouldReturnTheCorrectDriverClass(String browserClass,
            DriverManagerType driverManagerType) {
        assertThat(browserClass).isEqualTo(driverManagerType.browserClass());
    }

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("org.openqa.selenium.chrome.ChromeDriver", CHROME),
                Arguments.of("org.openqa.selenium.chrome.ChromeDriver",
                        CHROMIUM),
                Arguments.of("org.openqa.selenium.firefox.FirefoxDriver",
                        FIREFOX),
                Arguments.of("org.openqa.selenium.opera.OperaDriver", OPERA),
                Arguments.of("org.openqa.selenium.edge.EdgeDriver", EDGE),
                Arguments.of("org.openqa.selenium.phantomjs.PhantomJSDriver",
                        PHANTOMJS),
                Arguments.of("org.openqa.selenium.ie.InternetExplorerDriver",
                        IEXPLORER),
                Arguments.of("org.openqa.selenium.safari.SafariDriver",
                        SAFARI));
    }
}
