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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends BrowserManager {

	private static EdgeDriverManager instance;

	protected EdgeDriverManager() {
	}

	public static synchronized EdgeDriverManager getInstance() {
		if (instance == null) {
			instance = new EdgeDriverManager();
		}
		return instance;
	}

	@Override
	protected List<URL> getDrivers(String version) throws Exception {
		String edgeDriverUrl = WdmConfig.getString("wdm.edgeDriverUrl");
		log.debug("Reading {} to find out the latest version of Edge driver",
				edgeDriverUrl);

		// Switch off HtmlUnit logging
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
				.setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
				.setLevel(Level.OFF);

		// Using HtmlUnitDriver to read MSI package URL
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.manage()
				.timeouts()
				.implicitlyWait(WdmConfig.getInt("wdm.timeout"),
						TimeUnit.SECONDS);
		driver.get(edgeDriverUrl);
		driver.findElement(By.linkText("Download")).click();
		WebElement clickHere = driver.findElement(By.linkText("Click here"));
		String downloadLink = clickHere.getAttribute("href");
		List<URL> urlList = new ArrayList<>();
		urlList.add(new URL(downloadLink));
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
	protected String getDriverName() {
		return "MicrosoftWebDriver";
	}

}
