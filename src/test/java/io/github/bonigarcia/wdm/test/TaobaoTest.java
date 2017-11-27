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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

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
        ChromeDriverManager.getInstance().setup();

        File binary = new File(
                ChromeDriverManager.getInstance().getBinaryPath());
        assertTrue(binary.exists());
    }

    @Test(expected = WebDriverManagerException.class)
    public void testTaobaoException() {
        EdgeDriverManager.getInstance().useTaobaoMirror().setup();
        File binary = new File(EdgeDriverManager.getInstance().getBinaryPath());
        assertTrue(binary.exists());
    }

}
