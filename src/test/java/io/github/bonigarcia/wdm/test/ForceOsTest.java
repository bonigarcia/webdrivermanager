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

import static io.github.bonigarcia.wdm.OperatingSystem.LINUX;
import static io.github.bonigarcia.wdm.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.OperatingSystem.WIN;
import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.OperatingSystem;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
@RunWith(Parameterized.class)
public class ForceOsTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Parameter
    public OperatingSystem operatingSystem;

    @InjectMocks
    public Downloader downloader;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { WIN }, { LINUX }, { MAC } });
    }

    @Before
    public void setup() throws IOException {
        initMocks(this);
        cleanCache();
    }

    private void cleanCache() throws IOException {
        String cachePath = WebDriverManager.globalConfig().getTargetPath();
        cleanDirectory(new File(cachePath));
    }

    @After
    public void teardown() throws IOException {
        cleanCache();
    }

    @Test
    public void testForceOs() {
        chromedriver().operatingSystem(operatingSystem).setup();
        File binary = new File(chromedriver().getBinaryPath());
        log.debug("OS {} - binary path {}", operatingSystem, binary);
        assertTrue(binary.exists());
    }

}