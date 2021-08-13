/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Current version test.
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
class CurrentVersionTest {

    @ParameterizedTest
    @MethodSource("data")
    void testCurrentVersion(DriverManagerType driverManagerType, String url,
            String expectedVersion) throws Exception {
        WebDriverManager browserManager = WebDriverManager
                .getInstance(driverManagerType);

        Method method = WebDriverManager.class
                .getDeclaredMethod("getCurrentVersion", URL.class);
        method.setAccessible(true);

        String currentVersion = (String) method.invoke(browserManager,
                new URL(url));

        assertThat(currentVersion).isEqualTo(expectedVersion);
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of(CHROME,
                "https://chromedriver.storage.googleapis.com/81.0.4044.69/chromedriver_linux64.zip",
                "81.0.4044.69"),
                Arguments.of(EDGE,
                        "https://msedgedriver.azureedge.net/81.0.416.64/edgedriver_win64.zip",
                        "81.0.416.64"),
                Arguments.of(OPERA,
                        "https://github.com/operasoftware/operachromiumdriver/releases/download/v.81.0.4044.113/operadriver_win64.zip",
                        "81.0.4044.113"),
                Arguments.of(FIREFOX,
                        "https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-win64.zip",
                        "0.26.0"));
    }

}
