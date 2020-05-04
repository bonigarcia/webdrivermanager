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
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;

/**
 * Test for custom target.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.7.2
 */
public class CustomCacheTest {

    final Logger log = getLogger(lookup().lookupClass());

    Config globalConfig;

    @Before
    public void setup() {
        globalConfig = WebDriverManager.globalConfig();
    }

    @Test
    public void testCachePath() throws IOException {
        Path tmpFolder = createTempDirectory("").toRealPath();
        globalConfig.setCachePath(tmpFolder.toString());
        log.info("Using temporary folder {} as cache", tmpFolder);
        WebDriverManager.chromedriver().forceDownload().setup();
        String binaryPath = WebDriverManager.chromedriver().getBinaryPath();
        log.info("Binary path {}", binaryPath);
        assertThat(binaryPath, startsWith(tmpFolder.toString()));
        log.info("Deleting temporary folder {}", tmpFolder);
        WebDriverManager.chromedriver().clearCache();
    }

    @Test
    public void testCachePathContainsTilde() {
        String customPath = "C:\\user\\abcdef~1\\path";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath, startsWith(customPath));
    }

    @Test
    public void testCachePathStartsWithTildeSlash() {
        String customPath = "~/webdrivers";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath, startsWith(System.getProperty("user.home")));
    }

    @Test
    public void testCachePathStartsWithTilde() {
        String customPath = "~webdrivers";
        globalConfig.setCachePath(customPath);
        String cachePath = globalConfig.getCachePath();
        log.info("Using {} got {}", customPath, cachePath);
        assertThat(cachePath, startsWith(customPath));
    }

    @After
    public void teardown() throws IOException {
        globalConfig.reset();
    }
}
