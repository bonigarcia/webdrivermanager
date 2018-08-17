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

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.Downloader;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
@RunWith(MockitoJUnitRunner.class)
public class IgnoredVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @InjectMocks
    public Downloader downloader;

    @Before
    @After
    public void cleanCache() throws IOException {
        cleanDirectory(new File(downloader.getTargetPath()));
    }

    @Test
    public void testIgnoreVersions() {
        String[] ignoredVersions = { "2.33", "2.32" };
        chromedriver().ignoreVersions(ignoredVersions).setup();
        File binary = new File(chromedriver().getBinaryPath());
        log.debug("Using binary {} (ignoring {})", binary,
                Arrays.toString(ignoredVersions));

        for (String version : ignoredVersions) {
            assertThat(binary.getName(), not(containsString(version)));
        }
    }

}