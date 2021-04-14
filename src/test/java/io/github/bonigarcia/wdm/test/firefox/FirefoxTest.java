/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.firefox;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.test.base.BrowserTestParent;

/**
 * Test with Firefox browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.5.0
 */
public class FirefoxTest extends BrowserTestParent {

    @BeforeClass
    public static void setupClass() {
        assumeTrue(!IS_OS_WINDOWS);
        WebDriverManager.firefoxdriver().setup();
    }

    @Before
    public void setupTest() {
        driver = new FirefoxDriver();
    }

}
