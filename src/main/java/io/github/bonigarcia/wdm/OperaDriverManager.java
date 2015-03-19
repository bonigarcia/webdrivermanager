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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Manager for Opera.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaDriverManager extends BrowserManager {

	public static void setup(Architecture arch) {
		new OperaDriverManager().manage(arch);
	}

	public static void setup() {
		new OperaDriverManager().manage();
	}

	@Override
	protected List<URL> getDrivers(Architecture arch) throws Exception {
		URL driverUrl = WdmConfig.getUrl("wdm.operaDriverUrl");
		log.info("Connecting to {} to check lastest OperaDriver release",
				driverUrl);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				driverUrl.openStream()));

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		GitHubApi[] release = gson.fromJson(reader, GitHubApi[].class);

		latestVersion = release[0].getName();
		log.info("Latest driver version: {}", latestVersion);

		List<LinkedTreeMap<String, Object>> assets = release[0].getAssets();
		List<URL> urls = new ArrayList<URL>();
		for (LinkedTreeMap<String, Object> asset : assets) {
			urls.add(new URL(asset.get("browser_download_url").toString()));
		}

		if (WdmConfig.getBoolean("wdm.downloadJustForMySystem")) {
			urls = filter(arch, urls);
		}
		reader.close();
		return urls;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.operaDriverExport");
	}
}
