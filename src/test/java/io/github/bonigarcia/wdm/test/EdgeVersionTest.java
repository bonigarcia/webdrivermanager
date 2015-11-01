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

import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test asserting MicrosoftWebDriver Edge versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeVersionTest {

	@Test
	public void testLatestVersion() throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			EdgeDriverManager.getInstance().setup();
			String driverVersion = InternetExplorerDriverManager.getInstance()
					.getDownloadedVersion();
			Assert.assertNotNull(driverVersion);
		}
	}

	@Test
	public void testSpecificVersions() throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			String[] specificVersions = { "8D0D08CF-790D-4586-B726-C6469A9ED49C" };

			for (String specificVersion : specificVersions) {
				EdgeDriverManager.getInstance().setup(specificVersion);
				String driverVersion = InternetExplorerDriverManager
						.getInstance().getDownloadedVersion();

				Assert.assertEquals(specificVersion, driverVersion);
			}
		}
	}

}
