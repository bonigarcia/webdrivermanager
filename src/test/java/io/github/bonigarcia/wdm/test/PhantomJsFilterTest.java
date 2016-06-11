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
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Filter verifications for phantomjs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsFilterTest {

	protected static final Logger log = LoggerFactory
			.getLogger(PhantomJsFilterTest.class);

	protected BrowserManager phatomJsManager;
	protected List<URL> driversUrls;
	protected final String phantomJsBinaryName = "phantomjs";

	@Before
	public void setup() throws Exception {
		phatomJsManager = PhantomJsDriverManager.getInstance();
		driversUrls = phatomJsManager.getDrivers();
	}

	@Test
	public void testFilterPhantomJs() {
		List<URL> latestUrls = phatomJsManager.getLatest(driversUrls,
				Arrays.asList(phantomJsBinaryName));
		List<URL> filteredLatestUrls = phatomJsManager.filter(latestUrls,
				Architecture.x64);
		log.info("Filtered URLS for LATEST version {} : {}",
				phantomJsBinaryName, filteredLatestUrls);
		Assert.assertTrue(!filteredLatestUrls.isEmpty());

	}

	@Test
	public void testFilterVersionPhantomJs() {
		String specificVersion = "1.9.6";
		List<URL> specificVersionUrls = phatomJsManager.getVersion(driversUrls,
				Arrays.asList(phantomJsBinaryName), specificVersion);
		List<URL> filteredVersionUrls = phatomJsManager
				.filter(specificVersionUrls, Architecture.x64);
		log.info("Filtered URLS for {} version {}: {}", phantomJsBinaryName,
				specificVersion, filteredVersionUrls);
		Assert.assertTrue(!filteredVersionUrls.isEmpty());
	}

}
