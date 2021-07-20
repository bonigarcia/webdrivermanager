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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for custom target.
 *
 * @author Boni Garcia
 * @since 1.7.2
 */
class CustomCacheTest {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriverManager wdm = WebDriverManager.chromedriver();

    @Test
    void testCachePath() throws IOException {
        Path tmpFolder = createTempDirectory("").toRealPath();
        wdm.config().setCachePath(tmpFolder.toString());
        log.info("Using temporary folder {} as cache", tmpFolder);
        wdm.forceDownload().setup();
        String driverPath = wdm.getDownloadedDriverPath();
        log.info("Driver path {}", driverPath);
        assertThat(driverPath).startsWith(tmpFolder.toString());
        log.info("Deleting temporary folder {}", tmpFolder);
        wdm.clearDriverCache();
    }

    @Test
    void testCachePathContainsTilde() {
        String customPath = "C:\\user\\abcdef~1\\path";
        wdm.config().setCachePath(customPath);
        String cachePath = wdm.config().getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(customPath);
    }

    @Test
    void testCachePathStartsWithTildeSlash() {
        String customPath = "~/webdrivers";
        wdm.config().setCachePath(customPath);
        String cachePath = wdm.config().getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(System.getProperty("user.home"));
    }

    @Test
    void testCachePathStartsWithTilde() {
        String customPath = "~webdrivers";
        wdm.config().setCachePath(customPath);
        String cachePath = wdm.config().getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath).startsWith(customPath);
    }

}
