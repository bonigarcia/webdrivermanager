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
package io.github.bonigarcia.wdm.test.cli;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using WebDriverManager CLI to run browsers in Docker.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
@EnabledOnOs(LINUX)
class CliDockerTest {

    static final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "firefox" })
    void testInteractive(String browser) {
        assertTimeout(ofMinutes(5), () -> {
            ByteArrayInputStream intro = new ByteArrayInputStream(
                    "\r\n".getBytes());
            System.setIn(intro);
            log.debug("Running WebDriverManager CLI with arguments: {}",
                    browser);
            WebDriverManager.main(new String[] { "runInDocker=" + browser });
            log.debug("CLI test runing {} in Docker OK", browser);
        });
    }

}
