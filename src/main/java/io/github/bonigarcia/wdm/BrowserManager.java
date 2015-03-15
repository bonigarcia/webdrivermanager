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

import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public abstract class BrowserManager {

	protected static Logger log = LoggerFactory.getLogger(BrowserManager.class);

	private static final String SEPARATOR = "/";

	protected static String latestVersion = null;

	public static List<URL> filter(List<URL> list) {
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
			String myArchitecture = System.getProperty("sun.arch.data.model");
			for (URL url : list) {
				if (!url.getFile().contains(myArchitecture)
						&& out.contains(url)) {
					out.remove(url);
				}
			}
		}
		return out;
	}

	public static List<URL> getLatest(List<URL> list, String match) {
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

	public static Document loadXML(Reader reader) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(reader);
		return builder.parse(is);
	}
}
