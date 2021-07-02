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
package io.github.bonigarcia.wdm.test.interactive;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using wdm in interactive mode (from the shell).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.2
 */
class InteractiveTest {

    static final Logger log = getLogger(lookup().lookupClass());

    static final String EXT = IS_OS_WINDOWS ? ".exe" : "";

    @ParameterizedTest
    @MethodSource("data")
    void testInteractive(String argument, String driver) {
        log.debug("Running interactive wdm with arguments: {}", argument);
        WebDriverManager.main(new String[] { argument });
        File driverFile = new File(driver);
        boolean exists = driverFile.exists();
        boolean delete = driverFile.delete();
        assertThat(exists && delete);
        log.debug("Interactive test with {} OK", argument);
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("chrome", "chromedriver" + EXT),
                Arguments.of("firefox", "geckodriver" + EXT),
                Arguments.of("opera", "operadriver" + EXT),
                Arguments.of("edge", "msedgedriver" + EXT),
                Arguments.of("iexplorer", "IEDriverServer.exe"));
    }

}
