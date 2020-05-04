/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.bonigarcia.wdm.config.DriverManagerType;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Test the class name values from DriverManagerTypeTest
 *
 * @author Elias Nogueira (elias.nogueira@gmail.com)
 * @since 3.8.1
 */
@RunWith(Parameterized.class)
public class DriverManagerTypeTest {

    @Parameterized.Parameter
    public String browserClass;

    @Parameterized.Parameter(1)
    public DriverManagerType driverManagerType;

    @Test
    public void shouldReturnTheCorrectDriverClass() {
        assertEquals(browserClass, driverManagerType.browserClass());
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { "org.openqa.selenium.chrome.ChromeDriver", DriverManagerType.CHROME },
                { "org.openqa.selenium.chrome.ChromeDriver", DriverManagerType.CHROMIUM },
                { "org.openqa.selenium.firefox.FirefoxDriver", DriverManagerType.FIREFOX },
                { "org.openqa.selenium.opera.OperaDriver", DriverManagerType.OPERA },
                { "org.openqa.selenium.edge.EdgeDriver", DriverManagerType.EDGE },
                { "org.openqa.selenium.phantomjs.PhantomJSDriver", DriverManagerType.PHANTOMJS },
                { "org.openqa.selenium.ie.InternetExplorerDriver", DriverManagerType.IEXPLORER },
                { "org.openqa.selenium.safari.SafariDriver", DriverManagerType.SAFARI },
                { "org.openqa.selenium.remote.server.SeleniumServer", DriverManagerType.SELENIUM_SERVER_STANDALONE } });
    }
}
