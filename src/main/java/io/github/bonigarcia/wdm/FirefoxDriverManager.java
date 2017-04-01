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
 * Manager for Firefox (same as Marionette).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.5.0
 */
public class FirefoxDriverManager extends BrowserManager {

	public static synchronized BrowserManager getInstance() {
		if (instance == null
				|| !instance.getClass().equals(FirefoxDriverManager.class)) {
			instance = new FirefoxDriverManager();
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

			log.info("Reading {} to seek {}", driverUrl, getDriverName());

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

	protected GitHubApi getVersion(GitHubApi[] releaseArray, String version) {
		GitHubApi out = null;
		for (GitHubApi release : releaseArray) {
			if ((release.getName() != null
					&& release.getName().contains(version))
					|| (release.getTagName() != null
							&& release.getTagName().contains(version))) {
				out = release;
				break;
			}
		}
		return out;
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.geckoDriverExport");
	}

	@Override
	protected String getDriverVersionKey() {
		return "wdm.geckoDriverVersion";
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("wires", "geckodriver");
	}

	@Override
	protected String getDriverUrlKey() {
		return "wdm.geckoDriverUrl";
	}

	@Override
	protected String getCurrentVersion(URL url, String driverName)
			throws MalformedURLException {
		String currentVersion = url.getFile().substring(
				url.getFile().indexOf("-") + 1, url.getFile().lastIndexOf("-"));
		if (currentVersion.startsWith("v")) {
			currentVersion = currentVersion.substring(1);
		}
		return currentVersion;
	}

	@Override
	protected String preDownload(String target, String version)
			throws IOException {
		int iSeparator = target.indexOf(version) - 1;
		int iDash = target.lastIndexOf(version) + version.length();
		int iPoint = target.lastIndexOf("tar.gz") != -1
				? target.lastIndexOf(".tar.gz")
				: target.lastIndexOf(".gz") != -1 ? target.lastIndexOf(".gz")
						: target.lastIndexOf(".zip");
		target = target.substring(0, iSeparator + 1)
				+ target.substring(iDash + 1, iPoint).toLowerCase()
				+ target.substring(iSeparator);
		return target;
	}

	@Override
	public BrowserManager useTaobaoMirror() {
		try {
			driverUrl = new URL(
					WdmConfig.getString("wdm.geckoDriverTaobaoUrl"));
		} catch (MalformedURLException e) {
			log.error("Malformed URL", e);
			throw new RuntimeException(e);
		}
		return instance;
	}

	/**
	 * @since 1.6.2
	 */
	@Override
	protected boolean shouldCheckArchitecture(String driverName) {
		return !MY_OS_NAME.contains(OperativeSystem.mac.name());
	}
}
