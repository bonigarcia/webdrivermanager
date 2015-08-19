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

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.OperaDriverManager;

/**
 * Test with Opera browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaTest extends ManagerTest {

	@BeforeClass
	public static void setupClass() {
		OperaDriverManager.getInstance().setup();
	}

	@Before
	public void setupTest() {
		DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();
		if (SystemUtils.IS_OS_LINUX) {
			OperaOptions options = new OperaOptions();
			options.setBinary(new File("/usr/bin/opera"));
			capabilities.setCapability(OperaOptions.CAPABILITY, options);
		}
		driver = new OperaDriver(capabilities);
	}

	@After
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void testOpera() {
		browseWikipedia();
	}

}
