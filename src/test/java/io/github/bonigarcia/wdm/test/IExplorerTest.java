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

import static java.lang.Runtime.getRuntime;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.openqa.selenium.ie.InternetExplorerDriver;

import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.base.BaseBrowserTst;

/**
 * Test with Internet Explorer browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
@Ignore
public class IExplorerTest extends BaseBrowserTst {

    @BeforeClass
    public static void setupClass() {
        assumeTrue(IS_OS_WINDOWS);
        InternetExplorerDriverManager.getInstance().setup();
    }

    @Before
    public void setupTest() {
        driver = new InternetExplorerDriver();
    }

    @AfterClass
    public static void taskkill() throws IOException {
        getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
        getRuntime().exec("taskkill /F /IM iexplore.exe");
    }

}
