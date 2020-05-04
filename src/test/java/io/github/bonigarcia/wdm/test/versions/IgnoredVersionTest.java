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
package io.github.bonigarcia.wdm.test.versions;

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
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.etc.Config;
import io.github.bonigarcia.wdm.etc.WebDriverManagerException;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
public class IgnoredVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Before
    @After
    public void cleanCache() throws IOException {
        cleanDirectory(new File(new Config().getCachePath()));
    }

    @Test(expected = WebDriverManagerException.class)
    public void ignoredVersions() {
        String driverVersion = "81.0.4044.69";
        String[] ignoredVersions = { driverVersion };
        WebDriverManager.chromedriver().driverVersion(driverVersion)
                .ignoreDriverVersions(ignoredVersions).setup();
        File binary = new File(chromedriver().getBinaryPath());
        log.debug("Using binary {} (ignoring {})", binary,
                Arrays.toString(ignoredVersions));

        for (String v : ignoredVersions) {
            assertThat(binary.getName(), not(containsString(v)));
        }
    }

}
