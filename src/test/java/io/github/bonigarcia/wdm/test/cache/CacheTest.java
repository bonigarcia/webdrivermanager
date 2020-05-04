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

import static io.github.bonigarcia.wdm.etc.Architecture.DEFAULT;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.FIREFOX;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.cache.CacheFilter;
import io.github.bonigarcia.wdm.etc.Architecture;
import io.github.bonigarcia.wdm.etc.Config;
import io.github.bonigarcia.wdm.etc.DriverManagerType;

/**
 * Test for driver cache.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.5
 */
@RunWith(Parameterized.class)
public class CacheTest {

    @Parameter(0)
    public DriverManagerType driverManagerType;

    @Parameter(1)
    public String driverName;

    @Parameter(2)
    public String driverVersion;

    @Parameter(3)
    public Architecture arch;

    @Parameter(4)
    public String os;

    @Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { CHROME, "chromedriver", "81.0.4044.69", DEFAULT, "linux" },
                { FIREFOX, "geckodriver", "0.26.0", DEFAULT, "linux" } });
    }

    @Before
    @After
    public void cleanCache() throws IOException {
        String cachePath = new Config().getCachePath();
        cleanDirectory(new File(cachePath));
    }

    @Test
    public void testCache() throws Exception {
        WebDriverManager browserManager = WebDriverManager
                .getInstance(driverManagerType);
        browserManager.forceDownload().driverVersion(driverVersion).setup();

        CacheFilter cacheFilter = new CacheFilter(new Config());
        Optional<String> driverFromCache = cacheFilter.getDriverFromCache(
                driverVersion, driverName, driverManagerType, arch, os);
        assertThat(driverFromCache.get(), notNullValue());
    }

}
