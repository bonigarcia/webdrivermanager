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
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.WebDriverManagerException;

/**
 * Test for taobao.org mirror.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.6.1
 */
@RunWith(Parameterized.class)
public class TaobaoTest {

    @Parameter(0)
    public Class<? extends WebDriver> driverClass;

    @Parameter(1)
    public String taobaoUrl;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { ChromeDriver.class,
                "http://npm.taobao.org/mirrors/chromedriver/2.33/" } });
    }

    @Test
    public void testTaobao() throws Exception {
        setProperty("wdm.chromeDriverTaobaoUrl", taobaoUrl);

        BrowserManager browserManager = WebDriverManager
                .getInstance(driverClass);
        browserManager.useTaobaoMirror().setup();

        Method method = BrowserManager.class.getDeclaredMethod("getDriverUrl");
        method.setAccessible(true);
        URL driverUrl = (URL) method.invoke(browserManager);

        assertThat(driverUrl.toString(), containsString("taobao.org"));
    }

    @Test(expected = WebDriverManagerException.class)
    public void testTaobaoException() {
        EdgeDriverManager.getInstance().useTaobaoMirror().setup();
    }

}
