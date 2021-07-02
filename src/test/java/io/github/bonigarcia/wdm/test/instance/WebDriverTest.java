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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class WebDriverTest {

    @ParameterizedTest
    @ValueSource(classes = { ChromeDriver.class, FirefoxDriver.class,
            PhantomJSDriver.class })
    void testWebDriver(Class<? extends WebDriver> driverClass) {
        WebDriverManager.getInstance(driverClass).setup();
        String driverPath = WebDriverManager.getInstance(driverClass)
                .getDownloadedDriverPath();
        File driver = new File(driverPath);
        assertThat(driver).exists();
    }

}
