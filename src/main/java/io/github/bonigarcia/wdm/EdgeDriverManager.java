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
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.Downloader.createProxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends BrowserManager {

	private static EdgeDriverManager instance;

	public EdgeDriverManager() {
	}

	public static synchronized EdgeDriverManager getInstance() {
		if (instance == null) {
			instance = new EdgeDriverManager();
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws Exception {
		String edgeDriverUrl = WdmConfig.getString("wdm.edgeDriverUrl");
		log.debug("Reading {} to find out the latest version of Edge driver",
				edgeDriverUrl);

		Document doc = Jsoup.connect(edgeDriverUrl)
				.timeout((int) TimeUnit.SECONDS
						.toMillis(WdmConfig.getInt("wdm.timeout")))
				.proxy(createProxy()).get();

		Elements downloadLink = doc
				.select("ul.driver-downloads li.driver-download > a");
		Elements versionParagraph = doc.select(
				"ul.driver-downloads li.driver-download p.driver-download__meta");
		String[] latestVersion = versionParagraph.get(0).text().split(" ");

		versionToDownload = latestVersion[1];

		List<URL> urlList = new ArrayList<>();
		urlList.add(new URL(downloadLink.get(0).attr("href")));
		return urlList;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.edgeExport");
	}

	@Override
	protected String getDriverVersion() {
		return WdmConfig.getString("wdm.edgeVersion");
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return WdmConfig.getUrl("wdm.edgeDriverUrl");
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("MicrosoftWebDriver");
	}
}
