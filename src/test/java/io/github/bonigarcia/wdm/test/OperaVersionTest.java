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

import org.junit.Assert;
import org.junit.Test;

import io.github.bonigarcia.wdm.OperaDriverManager;

/**
 * Test asserting operadriver versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.2
 */
public class OperaVersionTest {

	@Test
	public void testLatestVersion() throws Exception {
		OperaDriverManager.getInstance().setup();
		String driverVersion = OperaDriverManager.getInstance()
				.getDownloadedVersion();
		Assert.assertNotNull(driverVersion);
	}

	@Test
	public void testSpecificVersions() throws Exception {
		String[] specificVersions = { "0.2.2", "0.2.0", "0.1.0" };

		for (String specificVersion : specificVersions) {
			OperaDriverManager.getInstance().setup(specificVersion);
			String driverVersion = OperaDriverManager.getInstance()
					.getDownloadedVersion();

			Assert.assertEquals(specificVersion, driverVersion);
		}
	}

}
