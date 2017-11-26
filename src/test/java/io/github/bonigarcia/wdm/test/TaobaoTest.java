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

import static java.lang.System.setProperty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Test;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManagerException;

/**
 * Test for taobao.org mirror.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.6.1
 */
public class TaobaoTest {

    @Test
    public void testTaobao() throws Exception {
        setProperty("wdm.chromeDriverTaobaoUrl",
                "http://npm.taobao.org/mirrors/chromedriver/2.33/");
        BrowserManager chromeDriver = ChromeDriverManager.getInstance();
        chromeDriver.useTaobaoMirror().setup();

        Method method = BrowserManager.class.getDeclaredMethod("getDriverUrl");
        method.setAccessible(true);
        URL driverUrl = (URL) method.invoke(chromeDriver);

        assertThat(driverUrl.toString(), containsString("taobao.org"));
    }

    @Test(expected = WebDriverManagerException.class)
    public void testTaobaoException() {
        EdgeDriverManager.getInstance().useTaobaoMirror().setup();
    }

    @AfterClass
    public static void teardown() throws MalformedURLException {
        ChromeDriverManager.getInstance().driverRepositoryUrl(
                new URL("https://chromedriver.storage.googleapis.com/"));
        EdgeDriverManager.getInstance().driverRepositoryUrl(new URL(
                "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/"));
    }

}
