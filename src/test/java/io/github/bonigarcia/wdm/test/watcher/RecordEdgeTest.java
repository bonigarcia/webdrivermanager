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
import static org.assertj.core.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

class RecordEdgeTest {

    static final Logger log = getLogger(lookup().lookupClass());

    static final int REC_TIMEOUT_SEC = 10;
    static final int POLL_TIME_MSEC = 100;
    static final String REC_FILENAME = "myRecordingEdge";
    static final String REC_EXT = ".webm";

    WebDriver driver;
    File targetFolder;
    WebDriverManager wdm = WebDriverManager.edgedriver().watch();

    @BeforeEach
    void setup() {
        driver = wdm.create();
        targetFolder = new File(System.getProperty("user.home"), "Downloads");
    }

    @AfterEach
    void teardown() {
        driver.quit();
    }

    @Test
    void test() throws InterruptedException {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/slow-calculator.html");

        wdm.startRecording(REC_FILENAME);

        // 1 + 3
        driver.findElement(By.xpath("//span[text()='1']")).click();
        driver.findElement(By.xpath("//span[text()='+']")).click();
        driver.findElement(By.xpath("//span[text()='3']")).click();
        driver.findElement(By.xpath("//span[text()='=']")).click();

        // ... should be 4, wait for it
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBe(By.className("screen"), "4"));

        wdm.stopRecording();

        long timeoutMs = System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(REC_TIMEOUT_SEC);

        File recFile;
        do {
            recFile = new File(targetFolder, REC_FILENAME + REC_EXT);
            if (System.currentTimeMillis() > timeoutMs) {
                fail("Timeout of " + REC_TIMEOUT_SEC
                        + " seconds waiting for recording " + recFile);
                break;
            }
            Thread.sleep(POLL_TIME_MSEC);

        } while (!recFile.exists());

        log.debug("Recording available at {}", recFile);
    }

}
