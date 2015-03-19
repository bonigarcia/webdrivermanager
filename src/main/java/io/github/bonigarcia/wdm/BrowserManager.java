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
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public abstract class BrowserManager {

	protected static final Logger log = LoggerFactory
			.getLogger(BrowserManager.class);

	private final String SEPARATOR = "/";

	protected String latestVersion = null;

	protected abstract List<URL> getDrivers(Architecture arch) throws Exception;

	protected abstract String getExportParameter();

	public void manage() {
		String myArchitecture = System.getProperty("sun.arch.data.model");
		manage(Architecture.valueOf("x" + myArchitecture));
	}

	public void manage(Architecture arch) {
		try {
			List<URL> urls = getDrivers(arch);
			List<URL> urlFilter = filter(arch, urls);

			for (URL url : urls) {
				String export = urlFilter.contains(url) ? getExportParameter()
						: null;
				new Downloader().download(url, latestVersion, export);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<URL> filter(Architecture arch, List<URL> list) {
		List<URL> out = new ArrayList<URL>();
		String mySystem = System.getProperty("os.name").toLowerCase();

		// Round #1 : Filter by OS
		for (URL url : list) {
			for (OperativeSystem os : OperativeSystem.values()) {
				if (mySystem.contains(os.name())
						&& url.getFile().toLowerCase().contains(os.name())) {
					out.add(url);
				}
			}
		}
		// Round #2 : Filter by architecture (32/64 bits)
		if (out.size() > 1) {
			for (URL url : list) {
				if (!url.getFile().contains(arch.toString())) {
					out.remove(url);
				}
			}
		}
		return out;
	}

	public List<URL> getLatest(List<URL> list, String match) {
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			if (url.getFile().contains(match)) {
				String currentVersion = url.getFile().substring(
						url.getFile().indexOf(SEPARATOR) + 1,
						url.getFile().lastIndexOf(SEPARATOR));
				if (latestVersion == null) {
					latestVersion = currentVersion;
					log.info("Latest driver version: {}", latestVersion);
				}
				if (url.getFile().contains(latestVersion)) {
					out.add(url);
				}
			}
		}
		return out;
	}

	public List<URL> getDriversFromXml(Architecture arch, URL driverUrl,
			String driverBinary) throws Exception {
		log.info("Connecting to {} to check lastest {} release", driverUrl,
				driverBinary);

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
		urls = getLatest(urls, driverBinary);

		if (WdmConfig.getBoolean("wdm.downloadJustForMySystem")) {
			urls = filter(arch, urls);
		}
		reader.close();
		return urls;
	}

	public Document loadXML(Reader reader) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(reader);
		return builder.parse(is);
	}
}
