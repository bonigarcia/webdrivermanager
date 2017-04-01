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

	public static synchronized BrowserManager getInstance() {
		if (instance == null
				|| !instance.getClass().equals(OperaDriverManager.class)) {
			instance = new OperaDriverManager();
		}
		return instance;
	}

	@Override
	protected List<URL> getDrivers() throws IOException {
		URL driverUrl = getDriverUrl();
		List<URL> urls;
		if (isUsingTaobaoMirror()) {
			urls = getDriversFromMirror(driverUrl);

		} else {
			String driverVersion = versionToDownload;

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(openGitHubConnection(driverUrl)))) {

				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();
				GitHubApi[] releaseArray = gson.fromJson(reader, GitHubApi[].class);
				if (driverVersion != null) {
					releaseArray = new GitHubApi[]{
						getVersion(releaseArray, driverVersion)};
				}

				urls = new ArrayList<>();
				for (GitHubApi release : releaseArray) {
					if (release != null) {
						List<LinkedTreeMap<String, Object>> assets = release
							.getAssets();
						for (LinkedTreeMap<String, Object> asset : assets) {
							urls.add(new URL(
								asset.get("browser_download_url").toString()));
						}
					}
				}
			}
		}
		return urls;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.operaDriverExport");
	}

	protected GitHubApi getVersion(GitHubApi[] releaseArray, String version) {
		GitHubApi out = null;
		for (GitHubApi release : releaseArray) {
			if (release.getName() != null
					&& release.getName().equalsIgnoreCase(version)) {
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
	protected String getDriverVersionKey() {
		return "wdm.operaDriverVersion";
	}

	@Override
	protected String getDriverUrlKey() {
		return "wdm.operaDriverUrl";
	}

	@Override
	protected String getCurrentVersion(URL url, String driverName)
			throws MalformedURLException {
		if (isUsingTaobaoMirror()) {
			int i = url.getFile().lastIndexOf(SEPARATOR);
			int j = url.getFile().substring(0, i).lastIndexOf(SEPARATOR) + 1;
			return url.getFile().substring(j, i);
		} else {
			return url.getFile().substring(
					url.getFile().indexOf(SEPARATOR + "v") + 2,
					url.getFile().lastIndexOf(SEPARATOR));
		}
	}

	@Override
	public BrowserManager useTaobaoMirror() {
		try {
			driverUrl = new URL(
					WdmConfig.getString("wdm.operaDriverTaobaoUrl"));
		} catch (MalformedURLException e) {
			log.error("Malformed URL", e);
			throw new RuntimeException(e);
		}
		return instance;
	}
}
