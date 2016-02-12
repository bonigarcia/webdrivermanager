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

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Filter test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.2
 */
public class FilterTest {

	protected static final Logger log = LoggerFactory
			.getLogger(FilterTest.class);

	@Test
	public void testFilterPhantomJs() throws Exception {
		BrowserManager phatomJsManager = PhantomJsDriverManager.getInstance();
		List<URL> driversUrls = phatomJsManager.getDrivers();
		String phantomJsBinaryName = "phantomjs";
		List<URL> candidateUrls = phatomJsManager.getLatest(driversUrls,
				phantomJsBinaryName);
		List<URL> filteredUrls = phatomJsManager.filter(candidateUrls,
				Architecture.x64);

		log.info("Filtered URLS for {} : {}", phantomJsBinaryName,
				filteredUrls);

		Assert.assertTrue(!filteredUrls.isEmpty());
	}

}
