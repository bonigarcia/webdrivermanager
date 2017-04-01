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
package io.github.bonigarcia.wdm.base;

import static io.github.bonigarcia.wdm.Architecture.DEFAULT;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;

/**
 * Parent class for version based tests.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.1
 */
@RunWith(Parameterized.class)
public abstract class BaseVersionTst {

	@Parameter
	public Architecture architecture;

	protected BrowserManager browserManager;
	protected String[] specificVersions;
	protected boolean validOS = true;

	protected static final Logger log = LoggerFactory
			.getLogger(BaseVersionTst.class);

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { Architecture.DEFAULT },
				{ Architecture.x32 }, { Architecture.x64 } });
	}

	@Test
	public void testLatestVersion() throws Exception {
		if (validOS) {
			if (architecture != DEFAULT) {
				browserManager.architecture(architecture).setup();
			} else {
				browserManager.setup();
			}

			String driverVersion = browserManager.getDownloadedVersion();
			Assert.assertNotNull(driverVersion);
		}
	}

	@Test
	public void testSpecificVersions() throws Exception {
		if (validOS) {
			for (String specificVersion : specificVersions) {
				log.info("Test specific version arch={} version={}",
						architecture, specificVersion);
				if (architecture != DEFAULT) {
					browserManager.architecture(architecture)
							.version(specificVersion).setup();
				} else {
					browserManager.version(specificVersion).setup();
				}
				String driverVersion = browserManager.getDownloadedVersion();

				Assert.assertEquals(specificVersion, driverVersion);
			}
		}
	}

}
