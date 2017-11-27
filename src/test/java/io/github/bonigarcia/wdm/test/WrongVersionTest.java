/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
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
