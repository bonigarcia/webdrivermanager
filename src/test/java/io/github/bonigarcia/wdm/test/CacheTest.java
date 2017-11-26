/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.Architecture.X32;
import static io.github.bonigarcia.wdm.Architecture.X64;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.Downloader;
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
    public Class<? extends WebDriver> driverClass;

    @Parameter(1)
    public String driverVersion;

    @Parameter(2)
    public Architecture architecture;

    @Parameters(name = "{index}: {0} {1} {2}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { ChromeDriver.class, "2.27", IS_OS_MAC ? X64 : X32 },
                { OperaDriver.class, "0.2.2", X64 },
                { PhantomJSDriver.class, "2.1.1", X64 },
                { FirefoxDriver.class, "0.17.0", X64 } });
    }

    @Before
    public void deleteDownloadedFiles() throws IOException {
        cleanDirectory(new File(new Downloader().getTargetPath()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCache() throws Exception {
        BrowserManager browserManager = WebDriverManager
                .getInstance(driverClass);
        browserManager.forceCache().forceDownload().architecture(architecture)
                .version(driverVersion).setup();
        Downloader downloader = new Downloader(browserManager);

        Method method = BrowserManager.class.getDeclaredMethod(
                "existsDriverInCache", String.class, String.class,
                Architecture.class);
        method.setAccessible(true);

        Optional<String> driverInChachePath = (Optional<String>) method.invoke(
                browserManager, downloader.getTargetPath(), driverVersion,
                architecture);

        assertThat(driverInChachePath.get(), notNullValue());
    }

}
