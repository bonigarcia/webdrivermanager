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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Constructor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Test browser new instance creation from DriverManagerTypeTest.
 *
 * @author Elias Nogueira (elias.nogueira@gmail.com)
 * @since 3.8.1
 */
public class NewInstanceFromDriverTypeTest {

    private static DriverManagerType driverManagerType = DriverManagerType.CHROME;
    private static WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.getInstance(driverManagerType).setup();
    }

    @Before
    public void setupTest() throws Exception {
        Constructor<?> declaredConstructor = Class
                .forName(driverManagerType.browserClass())
                .getDeclaredConstructor(ChromeOptions.class);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = (WebDriver) declaredConstructor.newInstance(options);
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
