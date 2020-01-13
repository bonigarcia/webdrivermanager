/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.OperatingSystem.WIN;
import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assume.assumeFalse;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManagerException;

/**
 * Test with Microsoft Edge using pre-installed version.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.0
 */
public class EdgePreInstalledTest {

    final Logger log = getLogger(lookup().lookupClass());

    File microsoftWebDriverFile = new File(System.getenv("SystemRoot"),
            "System32" + File.separator + "MicrosoftWebDriver.exe");

    @Test
    public void testInsiderExists() {
        assumeTrue(microsoftWebDriverFile.exists());
        exerciseEdgeInsider();
    }

    @Test(expected = WebDriverManagerException.class)
    public void testInsiderNotExists() {
        assumeFalse(microsoftWebDriverFile.exists());
        exerciseEdgeInsider();
    }

    private void exerciseEdgeInsider() {
        edgedriver().operatingSystem(WIN).version("pre-installed").setup();
        File binary = new File(edgedriver().getBinaryPath());
        log.debug("Edge driver {}", binary);
        assertTrue(binary.getName().endsWith(".exe"));
    }

}
