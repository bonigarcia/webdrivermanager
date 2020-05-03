/*
 * (C) Copyright 2019 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test latest version of edgedriver.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.6.0
 */
@RunWith(Parameterized.class)
public class LatestAndBetaTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Parameter
    public Class<? extends WebDriver> driverClass;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { ChromeDriver.class },
                { EdgeDriver.class } });
    }

    @Test
    public void testLatestAndBetaedgedriver() {
        WebDriverManager.getInstance(driverClass).avoidResolutionCache()
                .avoidBrowserDetection().operatingSystem(WIN).setup();
        String edgedriverStable = WebDriverManager.getInstance(driverClass)
                .getDownloadedVersion();
        log.debug("edgedriver LATEST version: {}", edgedriverStable);

        WebDriverManager.getInstance(driverClass).avoidResolutionCache()
                .avoidBrowserDetection().useBetaVersions().operatingSystem(WIN)
                .setup();
        String edgedriverBeta = WebDriverManager.getInstance(driverClass)
                .getDownloadedVersion();
        log.debug("edgedriver BETA version: {}", edgedriverBeta);

        assertThat(edgedriverStable, not(equalTo(edgedriverBeta)));
    }

}
