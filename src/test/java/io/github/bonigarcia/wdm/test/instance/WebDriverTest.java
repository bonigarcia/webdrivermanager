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
package io.github.bonigarcia.wdm.test.instance;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Parameterized test with several browsers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
@RunWith(Parameterized.class)
public class WebDriverTest {

    @Parameter
    public Class<? extends WebDriver> driverClass;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { ChromeDriver.class },
                { FirefoxDriver.class }, { PhantomJSDriver.class } });
    }

    @Test
    public void testWebDriver() {
        WebDriverManager.getInstance(driverClass).setup();
        String driverPath = WebDriverManager.getInstance(driverClass)
                .getDriverPath();
        File driver = new File(driverPath);
        assertTrue(driver.exists());
    }

}
