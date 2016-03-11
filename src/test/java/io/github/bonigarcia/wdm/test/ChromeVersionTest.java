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

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.base.BaseVersionTst;

/**
 * Test asserting chromedriver versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.1
 */
public class ChromeVersionTest extends BaseVersionTst {

	@Before
	public void setup() {
		browserManager = ChromeDriverManager.getInstance();
		specificVersions = new String[] { "2.10", "2.11", "2.12", "2.13",
				"2.14", "2.15", "2.16", "2.17", "2.18", "2.19", "2.20",
				"2.21" };
	}

}
