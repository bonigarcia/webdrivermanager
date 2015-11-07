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
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
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

	private static final String SEPARATOR = "/";
	private static final Architecture DEFAULT_ARCH = Architecture.valueOf("x"
			+ System.getProperty("sun.arch.data.model"));
	private static final String MY_OS_NAME = getOsName();
	private static final String VERSION_PROPERTY = "wdm.driverVersion";

	protected abstract List<URL> getDrivers(String version) throws Exception;

	protected abstract String getExportParameter();

	protected abstract String getDriverVersion();

	protected abstract String getDriverName();

	protected abstract URL getDriverUrl() throws MalformedURLException;

	protected String versionToDownload;

	public void setup() {
		setup(DEFAULT_ARCH, DriverVersion.NOT_SPECIFIED.name());
	}

	public void setup(String version) {
		setup(DEFAULT_ARCH, version);
	}

	public void setup(Architecture arch) {
		setup(arch, DriverVersion.NOT_SPECIFIED.name());
	}

	public void setup(Architecture arch, String version) {
		try {
			this.getClass()
					.newInstance()
					.manage(arch,
							version.equals(DriverVersion.NOT_SPECIFIED.name()) ? getDriverVersion()
									: version);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void manage(Architecture arch, DriverVersion version) {
		manage(arch, version.name());
	}

	public String existsDriverInCache(String repository, String driverName,
			String driverVersion) {
		log.trace("Checking if {} {} exists in cache {}", driverName,
				driverVersion, repository);

		Iterator<File> iterateFiles = FileUtils.iterateFiles(new File(
				repository), null, true);

		String driverInCache = null;
		while (iterateFiles.hasNext()) {
			driverInCache = iterateFiles.next().toString();
			if (driverInCache.contains(driverName)
					&& driverInCache.contains(driverVersion)) {
				log.debug("Found {} {} in cache: {} ", driverName,
						driverVersion, driverInCache);
				break;
			} else {
				driverInCache = null;
			}
		}

		if (driverInCache == null) {
			log.trace("{} {} do not exist in cache {}", driverName,
					driverVersion, repository);
		}
		return driverInCache;
	}

	public void manage(Architecture arch, String version) {
		try {
			boolean getLatest = version == null
					|| version.isEmpty()
					|| version.equalsIgnoreCase(DriverVersion.LATEST.name())
					|| version.equalsIgnoreCase(DriverVersion.NOT_SPECIFIED
							.name());

			String driverInCache = null;
			if (!getLatest) {
				driverInCache = existsDriverInCache(Downloader.getTargetPath(),
						getDriverName(), version);
			}

			if (driverInCache != null) {
				System.setProperty(VERSION_PROPERTY, version);
				exportDriver(getExportParameter(), driverInCache);

			} else {

				getDriverName();

				// Get the complete list of URLs
				List<URL> urls = getDrivers(version);
				List<URL> candidateUrls;
				boolean continueSearchingVersion;

				do {
					// Get the latest or concrete version
					if (getLatest) {
						candidateUrls = getLatest(urls, getDriverName());
					} else {
						candidateUrls = getVersion(urls, getDriverName(),
								version);
					}
					if (versionToDownload == null) {
						break;
					}

					if (this.getClass().equals(EdgeDriverManager.class)) {
						// Microsoft Edge binaries are different
						continueSearchingVersion = false;
					} else {
						// Filter by architecture and OS
						candidateUrls = filter(candidateUrls, arch);

						// Find out if driver version has been found or not
						continueSearchingVersion = candidateUrls.isEmpty()
								&& getLatest;
						if (continueSearchingVersion) {
							log.debug("No valid binary found for {} {}",
									getDriverName(), versionToDownload);
							urls = removeFromList(urls, versionToDownload);
							versionToDownload = null;
						}
					}

				} while (continueSearchingVersion);

				if (candidateUrls.isEmpty()) {
					String versionStr = getLatest ? "(latest version)"
							: version;
					String errMessage = getDriverName() + " " + versionStr
							+ " for " + MY_OS_NAME + arch.toString()
							+ " not found in " + getDriverUrl();
					log.error(errMessage);
					throw new RuntimeException(errMessage);
				}

				for (URL url : candidateUrls) {
					String export = candidateUrls.contains(url) ? getExportParameter()
							: null;
					System.setProperty(VERSION_PROPERTY, versionToDownload);
					Downloader.download(url, versionToDownload, export,
							getDriverName());
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<URL> filter(List<URL> list, Architecture arch) {
		log.trace("{} {} - URLs before filtering: {}", getDriverName(),
				versionToDownload, list);

		List<URL> out = new ArrayList<URL>();

		// Round #1 : Filter by OS
		for (URL url : list) {
			for (OperativeSystem os : OperativeSystem.values()) {
				if (MY_OS_NAME.contains(os.name())
						&& url.getFile().toLowerCase().contains(os.name())) {
					out.add(url);
				}
			}
		}

		log.trace("{} {} - URLs after filtering by OS ({}): {}",
				getDriverName(), versionToDownload, MY_OS_NAME, out);

		// Round #2 : Filter by architecture (32/64 bits)
		if (out.size() > 1) {
			for (URL url : list) {
				if (!url.getFile().contains(arch.toString())) {
					out.remove(url);
				}
			}
		}

		log.trace("{} {} - URLs after filtering by architecture ({}): {}",
				getDriverName(), versionToDownload, arch, out);

		return out;
	}

	public List<URL> removeFromList(List<URL> list, String version) {
		List<URL> out = new ArrayList<URL>(list);
		for (URL url : list) {
			if (url.getFile().contains(version)) {
				out.remove(url);
			}
		}
		return out;
	}

	public List<URL> getVersion(List<URL> list, String match, String version) {
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			if (url.getFile().contains(match)
					&& url.getFile().contains(version)) {
				out.add(url);
			}
		}
		versionToDownload = version;
		log.debug("Using {} {}", match, version);
		return out;
	}

	public List<URL> getLatest(List<URL> list, String match) {
		log.debug("Checking the lastest version of {}", match);
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			if (url.getFile().contains(match)) {
				String currentVersion = url.getFile().substring(
						url.getFile().indexOf(SEPARATOR) + 1,
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
		log.debug("Latest version of {} is {}", match, versionToDownload);
		return out;
	}

	public Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(
					Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	public List<URL> getDriversFromXml(URL driverUrl, String driverBinary)
			throws Exception {
		log.debug("Reading {} to seek {}", driverUrl, getDriverName());

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
		reader.close();

		return urls;
	}

	public Document loadXML(Reader reader) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(reader);
		return builder.parse(is);
	}

	public String getDownloadedVersion() {
		return System.getProperty(VERSION_PROPERTY);
	}

	private static String getOsName() {
		String os = System.getProperty("os.name").toLowerCase();

		if (SystemUtils.IS_OS_WINDOWS) {
			os = "win";
		} else if (SystemUtils.IS_OS_LINUX) {
			os = "linux";
		} else if (SystemUtils.IS_OS_MAC) {
			os = "mac";
		}
		return os;
	}

	protected static void exportDriver(String variableName, String variableValue) {
		log.debug("Exporting {} as {}", variableName, variableValue);
		System.setProperty(variableName, variableValue);
	}

}
