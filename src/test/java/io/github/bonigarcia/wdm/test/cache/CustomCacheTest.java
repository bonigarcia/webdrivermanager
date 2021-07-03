/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.cache;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;

/**
 * Test for custom target.
 *
 * @author Boni Garcia
 * @since 1.7.2
 */
class CustomCacheTest {

    final Logger log = getLogger(lookup().lookupClass());

    Config globalConfig;

    @BeforeEach
    void setup() {
        globalConfig = WebDriverManager.globalConfig();
    }

    @Test
    void testCachePath() throws IOException {
        Path tmpFolder = createTempDirectory("").toRealPath();
        globalConfig.setCachePath(tmpFolder.toString());
        log.info("Using temporary folder {} as cache", tmpFolder);
        WebDriverManager.chromedriver().forceDownload().setup();
        String driverPath = WebDriverManager.chromedriver()
                .getDownloadedDriverPath();
        log.info("Driver path {}", driverPath);
        assertThat(driverPath).startsWith(tmpFolder.toString());
        log.info("Deleting temporary folder {}", tmpFolder);
        WebDriverManager.chromedriver().clearDriverCache();
    }

    @Test
    void testCachePathContainsTilde() {
        String customPath = "C:\\user\\abcdef~1\\path";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(customPath);
    }

    @Test
    void testCachePathStartsWithTildeSlash() {
        String customPath = "~/webdrivers";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(System.getProperty("user.home"));
    }

    @Test
    void testCachePathStartsWithTilde() {
        String customPath = "~webdrivers";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(customPath);
    }

    @AfterEach
    void teardown() throws IOException {
        globalConfig.reset();
    }
}
