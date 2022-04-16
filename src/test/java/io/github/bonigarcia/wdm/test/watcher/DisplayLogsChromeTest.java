/*
 * (C) Copyright 2022 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.watcher;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

class DisplayLogsChromeTest {

    static final Logger log = getLogger(lookup().lookupClass());

    WebDriverManager wdm = WebDriverManager.chromedriver().watchAndDisplay();
    WebDriver driver;

    @BeforeEach
    void setup() {
        driver = wdm.create();
    }

    @AfterEach
    void teardown() throws InterruptedException {
        // pause for manual browser inspection
        Thread.sleep(Duration.ofSeconds(3).toMillis());

        driver.quit();
    }

    @Test
    void test() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/console-logs.html");

        List<Map<String, Object>> logMessages = wdm.getLogs();

        for (Map<String, Object> map : logMessages) {
            log.debug("[{}] [{}.{}] {}", map.get("datetime"),
                    map.get("wrapper").toString().toUpperCase(),
                    String.format("%1$-7s",
                            map.get("type").toString().toUpperCase()),
                    map.get("message"));
        }

        assertThat(logMessages).hasSize(5);
    }

}
