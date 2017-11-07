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

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
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

    // opera does not work with xvfb which is used on the ci server
    // see: https://github.com/operasoftware/operachromiumdriver/issues/26
    private static boolean ignoreTestInHeadlessEnvironment = "true"
            .equals(getProperty("headlessEnvironment"));

    @BeforeClass
    public static void setupClass() {
        if (ignoreTestInHeadlessEnvironment) {
            validOS = false;
            assumeTrue(validOS);
        }
        OperaDriverManager.getInstance().setup();
    }

    @Before
    public void setupTest() {
        DesiredCapabilities capabilities = operaBlink();
        File operaBinary = null;
        if (IS_OS_WINDOWS) {
            operaBinary = new File("C:\\Program Files\\Opera\\launcher.exe");
        } else {
            operaBinary = new File("/usr/bin/opera");
        }

        assumeTrue(operaBinary != null && operaBinary.exists());
        OperaOptions options = new OperaOptions();
        options.setBinary(operaBinary);
        capabilities.setCapability(CAPABILITY, options);

        driver = new OperaDriver(capabilities);
    }

}
