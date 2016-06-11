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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static OperaDriverManager instance;

	protected OperaDriverManager() {
	}

	public static synchronized OperaDriverManager getInstance() {
		if (instance == null) {
			instance = new OperaDriverManager();
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws IOException {
		URL driverUrl = getDriverUrl();
		String driverVersion = versionToDownload;

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(openGitHubConnection(driverUrl)));

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		GitHubApi[] releaseArray = gson.fromJson(reader, GitHubApi[].class);
		GitHubApi release;
		if (driverVersion == null || driverVersion.isEmpty() || driverVersion
				.equalsIgnoreCase(DriverVersion.LATEST.name())) {
			log.debug("Connecting to {} to check latest OperaDriver release",
					driverUrl);
			driverVersion = releaseArray[0].getName();
			log.debug("Latest driver version: {}", driverVersion);
			release = releaseArray[0];
		} else {
			release = getVersion(releaseArray, driverVersion);
		}
		if (release == null) {
			throw new RuntimeException("Version " + driverVersion
					+ " is not available for OperaDriver");
		}

		List<LinkedTreeMap<String, Object>> assets = release.getAssets();
		List<URL> urls = new ArrayList<>();
		for (LinkedTreeMap<String, Object> asset : assets) {
			urls.add(new URL(asset.get("browser_download_url").toString()));
		}

		reader.close();
		return urls;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.operaDriverExport");
	}

	private GitHubApi getVersion(GitHubApi[] releaseArray, String version) {
		GitHubApi out = null;
		for (GitHubApi release : releaseArray) {
			if (release.getName().equalsIgnoreCase(version)) {
				out = release;
				break;
			}
		}
		return out;
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("operadriver");
	}

	@Override
	protected String getDriverVersion() {
		return WdmConfig.getString("wdm.operaDriverVersion");
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return WdmConfig.getUrl("wdm.operaDriverUrl");
	}
}
