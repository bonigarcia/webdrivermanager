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
package io.github.bonigarcia.wdm.test.other;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThrows;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Parameterized test with several browsers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
@RunWith(Parameterized.class)
public class OtherWebDriverTest {

    @Parameter(0)
    public Class<? extends WebDriver> driverClass;

    @Parameter(1)
    public Class<? extends Throwable> exception;

    protected WebDriver driver;

    @Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                { EventFiringWebDriver.class, InstantiationException.class },
                { HtmlUnitDriver.class, null },
                { RemoteWebDriver.class, IllegalAccessException.class } });
    }

    @Before
    public void setupTest() {
        WebDriverManager.getInstance(driverClass).setup();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() {
        if (exception != null) {
            assertThrows(exception, this::createInstace);
        }

    }

    private void createInstace()
            throws InstantiationException, IllegalAccessException {
        driver = driverClass.newInstance();
    }

}
