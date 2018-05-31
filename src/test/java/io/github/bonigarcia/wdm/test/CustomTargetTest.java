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
package io.github.bonigarcia.wdm.test;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.Files.createTempDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
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

/**
 * Test for custom target.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.7.2
 */
public class CustomTargetTest {

    final Logger log = getLogger(lookup().lookupClass());

    Path tmpFolder;

    @Before
    public void setup() throws IOException {
        tmpFolder = createTempDirectory("").toRealPath();
        WebDriverManager.config().setTargetPath(tmpFolder.toString());
        log.info("Using temporal folder {} as cache", tmpFolder);
    }

    @Test
    public void testTargetPath() {
        WebDriverManager.chromedriver().setup();
        String binaryPath = WebDriverManager.chromedriver().getBinaryPath();
        log.info("Binary path {}", binaryPath);
        assertThat(binaryPath, startsWith(tmpFolder.toString()));
    }

    @After
    public void teardown() throws IOException {
        log.info("Deleting temporal folder {}", tmpFolder);
        deleteDirectory(tmpFolder.toFile());
        System.getProperties().remove("wdm.targetPath");
    }

}
