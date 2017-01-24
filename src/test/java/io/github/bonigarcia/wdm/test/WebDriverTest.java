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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.base.BaseBrowserTst;

/**
 * Parameterized test with several browsers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
@RunWith(Parameterized.class)
@Ignore
public class WebDriverTest extends BaseBrowserTst {

	@Parameter
	public Class<? extends WebDriver> driverClass;

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { ChromeDriver.class },
				{ FirefoxDriver.class } });
	}

	@Before
	public void setupTest()
			throws InstantiationException, IllegalAccessException {
		WebDriverManager.getInstance(driverClass).setup();
		driver = driverClass.newInstance();
	}

}
