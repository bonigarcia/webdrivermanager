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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ChromeDriverManager extends BrowserManager {

	private static ChromeDriverManager instance;

	public ChromeDriverManager() {
	}

	public static synchronized ChromeDriverManager getInstance() {
		if (instance == null) {
			instance = new ChromeDriverManager();
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws Exception {
		return getDriversFromXml(getDriverUrl(), getDriverName());
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.chromeDriverExport");
	}

	@Override
	protected String getDriverVersion() {
		return WdmConfig.getString("wdm.chromeDriverVersion");
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return WdmConfig.getUrl("wdm.chromeDriverUrl");
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("chromedriver");
	}

	@Override
	public String getCurrentVersion(URL url, String driverName)
			throws MalformedURLException {
		if (isUsingTaobaoMirror()) {
			int i = url.getFile().lastIndexOf(SEPARATOR);
			int j = url.getFile().substring(0, i).lastIndexOf(SEPARATOR) + 1;
			return url.getFile().substring(j, i);
		} else {
			return super.getCurrentVersion(url, driverName);
		}
	}

}
