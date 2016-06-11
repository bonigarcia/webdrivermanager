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
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
public class WebDriverManager extends BrowserManager {

	private static BrowserManager instance;

	private static Class<? extends BrowserManager> browserManagerClass;

	protected WebDriverManager() {
	}

	public static synchronized BrowserManager getInstance(
			Class<? extends WebDriver> webDriverClass) {
		if (instance == null) {
			if (webDriverClass.equals(ChromeDriver.class)) {
				browserManagerClass = ChromeDriverManager.class;

			} else if (webDriverClass.equals(OperaDriver.class)) {
				browserManagerClass = OperaDriverManager.class;

			} else if (webDriverClass.equals(InternetExplorerDriver.class)) {
				browserManagerClass = InternetExplorerDriverManager.class;

			} else if (webDriverClass.equals(EdgeDriver.class)) {
				browserManagerClass = EdgeDriverManager.class;

			} else if (webDriverClass.equals(PhantomJSDriver.class)) {
				browserManagerClass = PhantomJsDriverManager.class;

			} else if (webDriverClass.equals(MarionetteDriverManager.class)) {
				browserManagerClass = MarionetteDriverManager.class;

			} else {
				browserManagerClass = VoidDriverManager.class;
			}

			try {
				instance = browserManagerClass.newInstance();
			} catch (Throwable e) {
				String errMessage = "Error creating WebDriverManager";
				log.error(errMessage, e);
				throw new RuntimeException(errMessage, e);
			}
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws Exception {
		return instance.getDrivers();
	}

	@Override
	protected String getExportParameter() {
		return instance.getExportParameter();
	}

	@Override
	protected String getDriverVersion() {
		return instance.getDriverVersion();
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return instance.getDriverUrl();
	}

	@Override
	protected List<String> getDriverName() {
		return instance.getDriverName();
	}

}
