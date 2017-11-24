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

import org.junit.Before;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.base.BaseVersionTst;

/**
 * Test asserting MicrosoftWebDriver Edge versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeVersionTest extends BaseVersionTst {

    @Before
    public void setup() {
        browserManager = WebDriverManager.getInstance(EdgeDriver.class);
        specificVersions = new String[] { "4.15063" };
    }

}
