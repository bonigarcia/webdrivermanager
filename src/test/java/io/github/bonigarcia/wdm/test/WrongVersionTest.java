/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
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

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with incorrect version numbers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.7.2
 */
@RunWith(Parameterized.class)
public class WrongVersionTest {

    @Parameter(0)
    public Class<? extends WebDriver> driverClass;

    @Parameter(1)
    public String version;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { ChromeDriver.class, "99" },
                { FirefoxDriver.class, "99" } });
    }

    @Test
    public void testWrongVersionAndCache()
            throws InstantiationException, IllegalAccessException {
        WebDriverManager.getInstance(driverClass).setup();
        WebDriverManager.getInstance(driverClass).version(version).setup();
        File binary = new File(
                WebDriverManager.getInstance(driverClass).getBinaryPath());
        assertTrue(binary.exists());
    }

}
