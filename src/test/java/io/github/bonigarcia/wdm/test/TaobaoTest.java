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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;

import org.junit.Ignore;
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

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for taobao.org mirror.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.6.1
 */
@RunWith(Parameterized.class)
public class TaobaoTest {

    @Parameter
    public Class<? extends WebDriver> driverClass;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(
                new Object[][] { { ChromeDriver.class }, { OperaDriver.class },
                        { PhantomJSDriver.class }, { FirefoxDriver.class } });
    }

    @Test
    @Ignore("Taobao mirror sometimes raises a 'Connection reset' error")
    public void testCache() throws Exception {
        BrowserManager browserManager = WebDriverManager
                .getInstance(driverClass);
        browserManager.useTaobaoMirror().setup();

        Method method = BrowserManager.class.getDeclaredMethod("getDriverUrl");
        method.setAccessible(true);
        URL driverUrl = (URL) method.invoke(browserManager);

        assertThat(driverUrl.toString(), containsString("taobao.org"));
    }

}
