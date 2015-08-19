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

import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test asserting IEDriverServer versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.2
 */
public class IExplorerVersionTest {

	@Test
	public void testLatestVersion() throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			InternetExplorerDriverManager.getInstance().setup();
			String driverVersion = InternetExplorerDriverManager.getInstance().getDownloadedVersion();
			Assert.assertNotNull(driverVersion);
		}
	}

	@Test
	public void testSpecificVersions() throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			String[] specificVersions = { "2.39", "2.40", "2.41", "2.42", "2.43", "2.44", "2.45", "2.47" };

			for (String specificVersion : specificVersions) {
				InternetExplorerDriverManager.getInstance().setup(specificVersion);
				String driverVersion = InternetExplorerDriverManager.getInstance().getDownloadedVersion();

				Assert.assertEquals(specificVersion, driverVersion);
			}
		}
	}

}
