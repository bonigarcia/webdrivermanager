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
package io.github.bonigarcia.wdm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Manager for PhantomJs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsDriverManager extends BrowserManager {

	private static PhantomJsDriverManager instance;

	public PhantomJsDriverManager() {
	}

	public static synchronized PhantomJsDriverManager getInstance() {
		if (instance == null) {
			instance = new PhantomJsDriverManager();
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws Exception {
		String phantomjsDriverStr = WdmConfig
				.getString("wdm.phantomjsDriverUrl");
		log.debug(
				"Reading {} to find out the latest version of PhantomJS driver",
				phantomjsDriverStr);

		URL phantomjsDriverUrl = new URL(phantomjsDriverStr);
		String phantomjsDriverUrlContent = phantomjsDriverUrl.getPath();

		Document doc = Jsoup.connect(phantomjsDriverStr).get();
		Iterator<Element> iterator = doc.select("a").iterator();
		List<URL> urlList = new ArrayList<>();
		while (iterator.hasNext()) {
			String link = iterator.next().attr("href");
			if (link.startsWith(phantomjsDriverUrlContent)) {
				urlList.add(new URL(phantomjsDriverStr
						+ link.replace(phantomjsDriverUrlContent, "")));
			}
		}

		return urlList;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.phantomjsDriverExport");
	}

	@Override
	protected String getDriverVersion() {
		return WdmConfig.getString("wdm.phantomjsDriverVersion");
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return WdmConfig.getUrl("wdm.phantomjsDriverUrl");
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("phantomjs");
	}
}
