/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

/**
 * Test with Internet Explorer browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class IExplorerTest {

    protected static boolean validOS = true;

    protected WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        validOS = IS_OS_WINDOWS;

        if (validOS) {
            InternetExplorerDriverManager.getInstance().setup();
        }
    }

    @Before
    public void setupTest() {
        if (validOS) {
            driver = new InternetExplorerDriver();
        }
    }

    @Test
    public void test() {
        if (validOS) {
            driver.get("https://en.wikipedia.org/wiki/Main_Page");
        }
    }

    @After
    public void teardown() throws IOException {
        if (validOS) {
            Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
            Runtime.getRuntime().exec("taskkill /F /IM iexplore.exe");

            if (driver != null) {
                driver.close();
            }
        }
    }

}
