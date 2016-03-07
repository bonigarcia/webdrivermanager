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

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Test with PhatomJS.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsTest extends ManagerTest {

	@BeforeClass
	public static void setupClass() {
		PhantomJsDriverManager.getInstance().setup();
	}

	@Before
	public void setupTest() {
		PhantomJSDriverService service = new PhantomJSDriverService.Builder()
				.usingAnyFreePort()
				.usingPhantomJSExecutable(
						Paths.get(System.getProperty("phantomjs.binary.path"))
								.toFile())
				.usingCommandLineArguments(new String[] {
						"--ignore-ssl-errors=true", "--ssl-protocol=tlsv1",
						"--web-security=false", "--webdriver-loglevel=INFO" })
				.build();
		DesiredCapabilities desireCaps = new DesiredCapabilities();
		driver = new PhantomJSDriver(service, desireCaps);
	}

	@After
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void testPhantom() {
		browseWikipedia();
	}

}
