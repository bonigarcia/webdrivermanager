/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm.test;

import static java.lang.System.setProperty;
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

import io.github.bonigarcia.wdm.ChromeDriverManager;

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
        tmpFolder = createTempDirectory("");
        setProperty("wdm.targetPath", tmpFolder.toString());
        log.info("Using temporal folder {} as cache", tmpFolder);
    }

    @Test
    public void testTargetPath() {
        ChromeDriverManager.getInstance().setup();
        String binaryPath = ChromeDriverManager.getInstance().getBinaryPath();
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
