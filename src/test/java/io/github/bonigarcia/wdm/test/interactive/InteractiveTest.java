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
package io.github.bonigarcia.wdm.test.interactive;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using wdm in interactive mode (from the shell).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.2
 */
@RunWith(Parameterized.class)
public class InteractiveTest {

    public static final Logger log = getLogger(lookup().lookupClass());

    public static final String EXT = IS_OS_WINDOWS ? ".exe" : "";

    @Parameter(0)
    public String argument;

    @Parameter(1)
    public String driver;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { "chrome", "chromedriver" + EXT },
                { "firefox", "geckodriver" + EXT },
                { "opera", "operadriver" + EXT },
                { "edge", "msedgedriver.exe" },
                { "iexplorer", "IEDriverServer.exe" } });
    }

    @Test
    public void testInteractive() {
        log.debug("Running interactive wdm with arguments: {}", argument);
        WebDriverManager.main(new String[] { argument });
        File driverFile = new File(driver);
        boolean exists = driverFile.exists();
        boolean delete = driverFile.delete();
        assertTrue(exists && delete);
        log.debug("Interactive test with {} OK", argument);
    }

}
