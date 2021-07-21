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
package io.github.bonigarcia.wdm.test.server;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.openqa.selenium.net.PortProber.findFreePort;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using wdm server.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
@EnabledOnOs(LINUX)
class ServerSeleniumTest {

    static final Logger log = getLogger(lookup().lookupClass());

    static int serverPort;

    @BeforeAll
    static void startServer() throws IOException {
        serverPort = findFreePort();
        log.debug("Test is starting WebDriverManager server at port {}",
                serverPort);

        WebDriverManager
                .main(new String[] { "server", String.valueOf(serverPort) });
    }

    @ParameterizedTest
    @ValueSource(classes = { ChromeOptions.class, FirefoxOptions.class })
    void testServerSeleniumServer(Class<? extends Capabilities> capabilities)
            throws Exception {
        String serverUrl = String.format("http://localhost:%s/", serverPort);

        Capabilities caps = capabilities.getDeclaredConstructor().newInstance();
        WebDriver driver = new RemoteWebDriver(new URL(serverUrl), caps);

        String sutUrl = "https://github.com/bonigarcia/webdrivermanager";
        driver.get(sutUrl);
        String title = driver.getTitle();
        log.debug("The title of {} is {}", sutUrl, title);

        assertThat(title)
                .contains("Automated driver management for Selenium WebDriver");

        driver.close();
    }

}
