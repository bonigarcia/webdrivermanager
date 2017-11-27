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
package io.github.bonigarcia.wdm.test;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.openqa.selenium.opera.OperaOptions.CAPABILITY;
import static org.openqa.selenium.remote.DesiredCapabilities.operaBlink;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.base.BaseBrowserTst;

/**
 * Test with Opera browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaTest extends BaseBrowserTst {

    @BeforeClass
    public static void setupClass() {
        assumeFalse(parseBoolean(getProperty("headlessEnvironment")));
        OperaDriverManager.getInstance().setup();
    }

    @Before
    public void setupTest() {
        File operaBinary;
        if (IS_OS_WINDOWS) {
            operaBinary = new File("C:\\Program Files\\Opera\\launcher.exe");
        } else {
            operaBinary = new File("/usr/bin/opera");
        }
        assumeTrue(operaBinary.exists());

        OperaOptions options = new OperaOptions();
        options.setBinary(operaBinary);
        DesiredCapabilities capabilities = operaBlink();
        capabilities.setCapability(CAPABILITY, options);
        driver = new OperaDriver(capabilities);
    }

}
