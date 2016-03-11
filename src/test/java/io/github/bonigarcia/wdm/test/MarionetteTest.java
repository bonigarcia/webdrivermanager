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

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.MarionetteDriverManager;
import io.github.bonigarcia.wdm.base.BaseBrowserTst;

/**
 * Test with Marionette browser.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class MarionetteTest extends BaseBrowserTst {

	@BeforeClass
	public static void setupClass() {
		// FIXME: Marionette cannot be executed with other tests
		validOS = false;

		if (validOS) {
			MarionetteDriverManager.getInstance().setup();
		}
	}

	@Before
	public void setupTest() {
		if (validOS) {
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability("marionette", true);
			// This capability set need for beta\dev\nightly(version 45+)
			// firefox
			// because this driver is target on it
			if (IS_OS_LINUX) {
				capabilities.setCapability("binary", "/usr/bin/firefox");
			} else if (IS_OS_WINDOWS) {
				capabilities.setCapability("binary",
						"C:\\Program Files\\Mozilla Firefox\\firefox.exe");
			} else if (IS_OS_MAC_OSX) {
				capabilities.setCapability("binary",
						"/Applications/Firefox.app");
			}
			driver = new MarionetteDriver(capabilities);
		}
	}

}
