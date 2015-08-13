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

	protected static final Logger log = LoggerFactory.getLogger(BrowserManager.class);

	private final String SEPARATOR = "/";

	private static final String VERSION_PROPERTY = "wdm.driverVersion";

	private static final Architecture DEFAULT_ARCH = Architecture.valueOf("x"
			+ System.getProperty("sun.arch.data.model"));

	protected abstract List<URL> getDrivers(Architecture arch, String version) throws Exception;

	protected abstract String getExportParameter();

	protected String versionToDownload;

	public void manage(Architecture arch, DriverVersion version) {
		manage(arch, version.name());
	}

	public void manage(Architecture arch, String version) {
		try {
			List<URL> urls = getDrivers(arch, version);
			List<URL> urlFilter = filter(arch, urls);

			for (URL url : urls) {
				String export = urlFilter.contains(url) ? getExportParameter() : null;
				Downloader.download(url, versionToDownload, export);
			}
		} catch (RuntimeException re) {
			throw re;
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
				if (mySystem.contains(os.name()) && url.getFile().toLowerCase().contains(os.name())) {
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

	public List<URL> getVersion(List<URL> list, String match, String version) {
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			if (url.getFile().contains(match) && url.getFile().contains(version)) {
				out.add(url);
			}
		}
		if (out.isEmpty()) {
			throw new RuntimeException("Version " + version + " is not available for " + match);
		}
		versionToDownload = version;
		System.setProperty(VERSION_PROPERTY, versionToDownload);
		log.info("Using {} {}", match, version);
		return out;
	}

	public List<URL> getLatest(List<URL> list, String match) {
		log.info("Checking the lastest version of {}", match);
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			if (url.getFile().contains(match)) {
				String currentVersion = url.getFile().substring(url.getFile().indexOf(SEPARATOR) + 1,
						url.getFile().lastIndexOf(SEPARATOR));
				if (versionToDownload == null) {
					versionToDownload = currentVersion;
				}
				if (versionCompare(currentVersion, versionToDownload) > 0) {
					versionToDownload = currentVersion;
					out.clear();
				}
				if (url.getFile().contains(versionToDownload)) {
					out.add(url);
				}
			}
		}
		System.setProperty(VERSION_PROPERTY, versionToDownload);
		log.info("Using {} {}", match, versionToDownload);
		return out;
	}

	public Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	public List<URL> getDriversFromXml(Architecture arch, URL driverUrl, String driverBinary, String driverVersion)
			throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(driverUrl.openStream()));
		Document xml = loadXML(reader);

		List<URL> urls = new ArrayList<URL>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate("//Contents/Key", xml.getDocumentElement(), XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); ++i) {
			Element e = (Element) nodes.item(i);
			String version = e.getChildNodes().item(0).getNodeValue();
			urls.add(new URL(driverUrl + version));
		}

		if (driverVersion == null || driverVersion.isEmpty()
				|| driverVersion.equalsIgnoreCase(DriverVersion.LATEST.name())) {
			urls = getLatest(urls, driverBinary);
		} else {
			urls = getVersion(urls, driverBinary, driverVersion);
		}

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

	public void setup() {
		try {
			this.getClass().newInstance().manage(DEFAULT_ARCH, DriverVersion.NOT_SPECIFIED);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void setup(Architecture arch, String version) {
		try {
			this.getClass().newInstance().manage(arch, version);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void setup(String version) {
		try {
			this.getClass().newInstance().manage(DEFAULT_ARCH, version);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void setup(Architecture arch) {
		try {
			this.getClass().newInstance().manage(arch, DriverVersion.NOT_SPECIFIED);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String getDriverVersion() {
		return System.getProperty(VERSION_PROPERTY);
	}

}
