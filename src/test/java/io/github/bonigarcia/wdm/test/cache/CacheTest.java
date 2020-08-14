/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.config.Architecture.DEFAULT;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.cache.CacheHandler;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Test for driver cache.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.5
 */
@RunWith(Parameterized.class)
public class CacheTest {

    static final Logger log = getLogger(lookup().lookupClass());

    @Parameter(0)
    public DriverManagerType driverManagerType;

    @Parameter(1)
    public String driverName;

    @Parameter(2)
    public String driverVersion;

    @Parameter(3)
    public Architecture arch;

    @Parameter(4)
    public OperatingSystem os;

    @Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { CHROME, "chromedriver", "81.0.4044.69", DEFAULT, LINUX },
                { FIREFOX, "geckodriver", "0.26.0", DEFAULT, LINUX } });
    }

    @Before
    @After
    public void cleanCache() {
        WebDriverManager.chromedriver().clearDriverCache();
    }

    @Test
    public void testCache() throws Exception {
        WebDriverManager browserManager = WebDriverManager
                .getInstance(driverManagerType);
        browserManager.clearResolutionCache().forceDownload()
                .operatingSystem(os).driverVersion(driverVersion).setup();

        CacheHandler cacheHandler = new CacheHandler(new Config());
        Optional<String> driverFromCache = cacheHandler.getDriverFromCache(
                driverVersion, driverName, driverManagerType, arch, os.name());

        log.debug("Driver from cache: {}", driverFromCache);
        assertThat(driverFromCache.get(), notNullValue());
    }

}
