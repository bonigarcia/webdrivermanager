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
package io.github.bonigarcia.wdm.test.forced;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Force download test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.1
 */
@RunWith(Parameterized.class)
public class ForceDownloadTest {

    static final int TIMEOUT = 20;
    static final OperatingSystem OS = OperatingSystem.WIN;

    @Parameter
    public Class<? extends WebDriver> driverClass;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { ChromeDriver.class },
                { FirefoxDriver.class }, { OperaDriver.class },
                { EdgeDriver.class } });
    }

    @Test
    public void testForceDownload() {
        WebDriverManager driverManager = WebDriverManager
                .getInstance(driverClass);
        driverManager.forceDownload().avoidBrowserDetection()
                .avoidReadReleaseFromRepository().timeout(TIMEOUT)
                .operatingSystem(OS).setup();
        assertThat(driverManager.getDownloadedDriverPath(), notNullValue());
    }

}
