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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manager for Internet Explorer.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class InternetExplorerDriverManager extends BrowserManager {

	public static void setup() {
		try {
			URL driverUrl = new URL(
					Config.getProperty("internetExplorerDriverUrl"));
			log.debug("Connecting to {} to check lastest ChromeDriver release",
					driverUrl);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					driverUrl.openStream()));
			Document xml = loadXML(reader);

			List<URL> urls = new ArrayList<URL>();
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xPath.evaluate("//Contents/Key",
					xml.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); ++i) {
				Element e = (Element) nodes.item(i);
				String version = e.getChildNodes().item(0).getNodeValue();
				urls.add(new URL(driverUrl + version));
			}

			urls = getLatest(urls, "IEDriverServer");

			if (Boolean.parseBoolean(Config
					.getProperty("downloadJustForMySystem"))) {
				urls = filter(urls);
			}

			for (URL url : urls) {
				Downloader.download(url,
						Config.getProperty("internetExplorerExport"));
			}
			reader.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
