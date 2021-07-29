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
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

class DockerCustomImageTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver;

    WebDriverManager wdm = WebDriverManager.chromedriver().browserInDocker()
            .dockerImage("selenoid/vnc:chrome_90.0").enableVnc()
            .enableRecording();

    @BeforeEach
    void setupTest() {
        driver = wdm.create();
    }

    @AfterEach
    void teardown() {
        wdm.quit();
    }

    @Test
    void test() throws Exception {
        String sutUrl = "https://bonigarcia.org/webdrivermanager";
        driver.get(sutUrl);
        String title = driver.getTitle();
        log.debug("The title of {} is {}", sutUrl, title);
        Wait<WebDriver> wait = new WebDriverWait(driver,
                Duration.ofSeconds(30));
        wait.until(d -> d.getTitle().contains("WebDriverManager"));

        URL dockerSessionUrl = wdm.getDockerNoVncUrl();
        HttpURLConnection huc = (HttpURLConnection) dockerSessionUrl
                .openConnection();
        assertThat(huc.getResponseCode()).isEqualTo(HTTP_OK);

        // Active wait for manual inspection
        Thread.sleep(Duration.ofSeconds(10).toMillis());

        Path recordingPath = wdm.getDockerRecordingPath();
        assertThat(recordingPath).exists();
    }

}
