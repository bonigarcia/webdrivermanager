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

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
@Ignore
public class OperaTest extends BaseBrowserTst {

	// opera does not work with xvfb which is used on the ci server
	// see: https://github.com/operasoftware/operachromiumdriver/issues/26
	private static boolean ignoreTestInHeadlessEnvironment = "true"
			.equals(System.getProperty("headlessEnvironment"));

	@BeforeClass
	public static void setupClass() {
		if (ignoreTestInHeadlessEnvironment) {
			validOS = false;
			assumeTrue(false);
		}
		OperaDriverManager.getInstance().setup();
	}

	@Before
	public void setupTest() {
		DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();
		if (IS_OS_LINUX) {
			assumeTrue("no Opera installed on Linux; well ... :-)",
					new File("/usr/bin/opera").exists());
			OperaOptions options = new OperaOptions();
			options.setBinary(new File("/usr/bin/opera"));
			capabilities.setCapability(OperaOptions.CAPABILITY, options);
		}
		driver = new OperaDriver(capabilities);
	}

}
