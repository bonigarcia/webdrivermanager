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
package io.github.bonigarcia.wdm.test.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.ChromiumDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;

class GenericBrowserNameTest {

    @ParameterizedTest
    @MethodSource("data")
    void test(String browserName,
            Class<? extends WebDriverManager> managerClass) {
        WebDriverManager wdm = WebDriverManager.getInstance(browserName);
        assertThat(wdm.getClass()).isEqualTo(managerClass);
    }

    @Test
    void testError() {
        assertThrows(WebDriverManagerException.class,
                () -> WebDriverManager.getInstance(""));
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("chrome", ChromeDriverManager.class),
                Arguments.of("Chrome", ChromeDriverManager.class),
                Arguments.of("firefox", FirefoxDriverManager.class),
                Arguments.of("FIREFOX", FirefoxDriverManager.class),
                Arguments.of("edge", EdgeDriverManager.class),
                Arguments.of("msedge", EdgeDriverManager.class),
                Arguments.of("MicrosoftEdge", EdgeDriverManager.class),
                Arguments.of("opera", OperaDriverManager.class),
                Arguments.of("operablink", OperaDriverManager.class),
                Arguments.of("chromium", ChromiumDriverManager.class),
                Arguments.of("iexplorer", InternetExplorerDriverManager.class),
                Arguments.of("internet explorer",
                        InternetExplorerDriverManager.class));
    }

}
