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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Manager for PhantomJs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsDriverManager extends BrowserManager {

	public static synchronized BrowserManager getInstance() {
		if (instance == null
				|| !instance.getClass().equals(PhantomJsDriverManager.class)) {
			instance = new PhantomJsDriverManager();
		}
		return instance;
	}

	@Override
	protected List<URL> getDrivers() throws Exception {
		return getDriversFromMirror(getDriverUrl());
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.phantomjsDriverExport");
	}

	@Override
	protected String getDriverVersionKey() {
		return "wdm.phantomjsDriverVersion";
	}

	@Override
	protected String getDriverUrlKey() {
		return "wdm.phantomjsDriverUrl";
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("phantomjs");
	}

	@Override
	protected String getCurrentVersion(URL url, String driverName)
			throws MalformedURLException {
		String file = url.getFile();
		file = url.getFile().substring(file.lastIndexOf(SEPARATOR),
				file.length());
		final int matchIndex = file.indexOf(driverName);
		String currentVersion = file
				.substring(matchIndex + driverName.length() + 1, file.length());
		final int dashIndex = currentVersion.indexOf('-');
		currentVersion = currentVersion.substring(0, dashIndex);
		return currentVersion;
	}

	@Override
	protected String preDownload(String target, String version)
			throws IOException {
		int iSeparator = target.indexOf(version) - 1;
		int iDash = target.lastIndexOf(version) + version.length();
		int iPoint = target.lastIndexOf(".tar") != -1
				? target.lastIndexOf(".tar") : target.lastIndexOf(".zip");
		target = target.substring(0, iSeparator + 1)
				+ target.substring(iDash + 1, iPoint)
				+ target.substring(iSeparator);
		target = target.replace("beta-", "");
		return target;
	}

	@Override
	protected File postDownload(File archive) throws IOException {
		log.trace("PhatomJS package name: {}", archive);

		File extractFolder = archive.getParentFile().listFiles()[0];
		log.trace("PhatomJS extract folder (to be deleted): {}", extractFolder);

		File binFolder = new File(
				extractFolder.getAbsoluteFile() + File.separator + "bin");
		// Exception for older version of PhantomJS
		int binaryIndex = 0;
		if (!binFolder.exists()) {
			binFolder = extractFolder;
			binaryIndex = 3;
		}

		log.trace("PhatomJS bin folder: {} (index {})", binFolder, binaryIndex);

		File phantomjs = binFolder.listFiles()[binaryIndex];
		log.trace("PhatomJS binary: {}", phantomjs);

		File target = new File(archive.getParentFile().getAbsolutePath()
				+ File.separator + phantomjs.getName());
		log.trace("PhatomJS target: {}", target);

		phantomjs.renameTo(target);
		FileUtils.deleteDirectory(extractFolder);
		return target;
	}

	@Override
	public BrowserManager useTaobaoMirror() {
		try {
			driverUrl = new URL(
					WdmConfig.getString("wdm.phantomjsDriverTaobaoUrl"));
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
		return false;
	}
}
