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

import static io.github.bonigarcia.wdm.Architecture.x32;
import static io.github.bonigarcia.wdm.Architecture.x64;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Test for driver cache.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.5
 */
@RunWith(Parameterized.class)
public class CacheTest {

	@Parameter(0)
	public Class<? extends BrowserManager> browserManagerClass;

	@Parameter(1)
	public String driverVersion;

	@Parameter(2)
	public Architecture architecture;

	@Parameters(name = "{index}: {0} {1} {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(
				new Object[][] { { ChromeDriverManager.class, "2.23", x32 },
						{ OperaDriverManager.class, "0.2.2", x64 },
						{ PhantomJsDriverManager.class, "2.1.1", x64 },
						{ FirefoxDriverManager.class, "0.10.0", x64 } });
	}

	@Test
	public void testCache() throws Exception {
		BrowserManager browserManager = browserManagerClass.newInstance();
		browserManager.setup(architecture, driverVersion);
		String driverInChachePath = browserManager.existsDriverInCache(
				Downloader.getTargetPath(), driverVersion, architecture);

		assertThat(driverInChachePath, notNullValue());
	}

}
