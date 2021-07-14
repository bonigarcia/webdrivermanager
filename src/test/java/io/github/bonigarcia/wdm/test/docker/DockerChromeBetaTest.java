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
package io.github.bonigarcia.wdm.test.docker;

import static java.lang.invoke.MethodHandles.lookup;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with Chrome beta in Docker.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
@EnabledOnOs(LINUX)
class DockerChromeBetaTest {

    final Logger log = getLogger(lookup().lookupClass());

    static final int MANUAL_INSPECTION_WAIT_TIME_SEC = 10;

    WebDriver driver;

    @BeforeEach
    void setupTest() {
        driver = WebDriverManager.chromedriver().browserInDocker()
                .browserVersion("beta").enableVnc().create();
    }

    @AfterEach
    void teardown() {
        WebDriverManager.chromedriver().quit();
    }

    @Test
    void test() throws Exception {
        String sutUrl = "https://github.com/bonigarcia/webdrivermanager";
        driver.get(sutUrl);
        String title = driver.getTitle();
        log.debug("The title of {} is {}", sutUrl, title);
        assertThat(title)
                .contains("Automated driver management for Selenium WebDriver");

        URL dockerSessionUrl = WebDriverManager.chromedriver()
                .getDockerNoVncUrl();
        HttpURLConnection huc = (HttpURLConnection) dockerSessionUrl
                .openConnection();
        assertThat(huc.getResponseCode()).isEqualTo(HTTP_OK);

        // Active wait to manually inspect
        Thread.sleep(SECONDS.toMillis(MANUAL_INSPECTION_WAIT_TIME_SEC));
    }

}
