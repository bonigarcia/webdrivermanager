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
package io.github.bonigarcia.wdm.test;

import static io.github.bonigarcia.wdm.Architecture.X32;
import static io.github.bonigarcia.wdm.Architecture.X64;
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.DriverManagerType.PHANTOMJS;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;

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
    public String driverVersion;

    @Parameter(2)
    public Architecture architecture;

    @Parameters(name = "{index}: {0} {1} {2}")
    public static Collection<Object[]> data() {
        return asList(
                new Object[][] { { CHROME, "2.27", IS_OS_MAC ? X64 : X32 },
                        { OPERA, "0.2.2", X64 }, { PHANTOMJS, "2.1.1", X64 },
                        { FIREFOX, "0.17.0", X64 } });
    }

    @Before
    @After
    public void cleanCache() throws IOException {
        cleanDirectory(
                new File(new Downloader(driverManagerType).getTargetPath()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCache() throws Exception {
        WebDriverManager browserManager = WebDriverManager
                .getInstance(driverManagerType);
        browserManager.forceCache().forceDownload().architecture(architecture)
                .version(driverVersion).setup();

        Method method = WebDriverManager.class.getDeclaredMethod(
                "getDriverFromCache", String.class, Architecture.class,
                String.class);
        method.setAccessible(true);

        Optional<String> driverInCachePath = (Optional<String>) method
                .invoke(browserManager, driverVersion, architecture, "");

        assertThat(driverInCachePath.get(), notNullValue());
    }

}
