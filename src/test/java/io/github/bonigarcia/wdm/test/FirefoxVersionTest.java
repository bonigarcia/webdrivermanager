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

import org.junit.Before;

import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.base.BaseVersionTst;

/**
 * Test asserting Marionette versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.5.0
 */
public class FirefoxVersionTest extends BaseVersionTst {

	@Before
	public void setup() {
		browserManager = FirefoxDriverManager.getInstance();
		specificVersions = new String[] { "0.10.0", "0.9.0", "0.8.0", "0.6.2",
				"0.5.0", "0.4.0", "0.3.0" };
	}

}
