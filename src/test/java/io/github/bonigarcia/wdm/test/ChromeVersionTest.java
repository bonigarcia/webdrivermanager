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

import io.github.bonigarcia.wdm.ChromeDriverManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test asserting chromedriver versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.1
 */
public class ChromeVersionTest {

	@Ignore
	@Test
	public void testLatestVersion() throws Exception {
		ChromeDriverManager.getInstance().setup();
		String driverVersion = ChromeDriverManager.getInstance().getDriverVersion();

		URL latest = new URL("http://chromedriver.storage.googleapis.com/LATEST_RELEASE");
		BufferedReader in = new BufferedReader(new InputStreamReader(latest.openStream()));
		String latestVersion = in.readLine();
		in.close();

		Assert.assertEquals(latestVersion, driverVersion);
	}

	@Test
	public void testSpecificVersions() throws Exception {
		String[] specificVersions = { "2.10", "2.11", "2.12", "2.13", "2.14", "2.15", "2.16", "2.17" };

		for (String specificVersion : specificVersions) {
			ChromeDriverManager.getInstance().setup(specificVersion);
			String driverVersion = ChromeDriverManager.getInstance().getDriverVersion();

			Assert.assertEquals(specificVersion, driverVersion);
		}
	}

}
