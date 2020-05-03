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

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test browser new instance creation from DriverManagerTypeTest.
 *
 * @author Elias Nogueira (elias.nogueira@gmail.com)
 * @since 3.8.1
 */
public class NewInstanceFromDriverType  {

    private static DriverManagerType driverManagerType = DriverManagerType.CHROME;
    private static WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.getInstance(driverManagerType).setup();
    }

    @Before
    public void setupTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> driverClass =  Class.forName(driverManagerType.browserClass());
        driver = (WebDriver) driverClass.newInstance();
    }

    @Test
    public void createNewChromeInstanceFromDriverManagerType() {
        assertThat(driver, instanceOf(ChromeDriver.class));
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}
