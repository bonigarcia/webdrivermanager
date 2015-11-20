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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Downloader class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class Downloader {
	protected static final Logger log = LoggerFactory
			.getLogger(Downloader.class);

	private static final String HOME = "~";

	public static final synchronized void download(URL url, String version,
			String export, String driverName) throws IOException {
		File targetFile = new File(getTarget(version, url));
		File binary = null;

		// Check if binary exists
		boolean download = !targetFile.getParentFile().exists()
				|| WdmConfig.getBoolean("wdm.override");

		if (!download) {
			// Check if existing binary is valid
			Collection<File> listFiles = FileUtils
					.listFiles(targetFile.getParentFile(), null, true);
			for (File file : listFiles) {
				if (file.getName().startsWith(driverName)
						&& file.canExecute()) {
					binary = file;
					log.debug("Using binary driver previously downloaded {}",
							binary);
					break;
				} else {
					download = true;
				}
			}
		}

		if (download) {
			log.info("Downloading {} to {}", url, targetFile);
			FileUtils.copyURLToFile(url, targetFile);

			if (export.contains("edge")) {
				binary = extractMsi(targetFile);
			} else {
				binary = unZip(targetFile);
			}
			targetFile.delete();
		}
		if (export != null) {
			BrowserManager.exportDriver(export, binary.toString());
		}

	}

	public static final File extractMsi(File msi) throws IOException {
		File tmpMsi = new File(Files.createTempDir().getAbsoluteFile()
				+ File.separator + msi.getName());
		Files.move(msi, tmpMsi);
		log.trace("Temporal msi file: {}", tmpMsi);

		Process process = Runtime.getRuntime()
				.exec(new String[] { "msiexec", "/a", tmpMsi.toString(), "/qb",
						"TARGETDIR=" + msi.getParent() });
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			log.error("Exception waiting to msiexec to be finished", e);
		} finally {
			process.destroy();
		}

		tmpMsi.delete();

		Collection<File> listFiles = FileUtils.listFiles(
				new File(msi.getParent()), new String[] { "exe" }, true);
		return listFiles.iterator().next();
	}

	public static final File unZip(File folder) throws IOException {
		ZipFile zipFolder = new ZipFile(folder);
		Enumeration<?> enu = zipFolder.entries();
		File file = null;

		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();

			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			log.trace("Unzipping {} (size: {} KB, compressed size: {} KB)",
					name, size, compressedSize);

			file = new File(folder.getParentFile() + File.separator + name);
			if (!file.exists() || WdmConfig.getBoolean("wdm.override")) {
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				InputStream is = zipFolder.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
				}
				is.close();
				fos.close();
				file.setExecutable(true);
			} else {
				log.debug(file + " already exists");
			}

		}
		zipFolder.close();

		return file.getAbsoluteFile();
	}

	private static final String getTarget(String version, URL url)
			throws IOException {
		String zip = url.getFile().substring(url.getFile().lastIndexOf("/"));

		int iFirst = zip.indexOf("_");
		int iLast = iFirst != zip.lastIndexOf("_") ? zip.lastIndexOf("_")
				: zip.length();
		String folder = zip.substring(0, iLast).replace(".zip", "")
				.replace(".msi", "").replace("_", File.separator);

		return getTargetPath() + folder + File.separator + version
				+ File.separator + zip;
	}

	protected static String getTargetPath() {
		String targetPath = WdmConfig.getString("wdm.targetPath");
		if (targetPath.contains(HOME)) {
			targetPath = targetPath.replace(HOME,
					System.getProperty("user.home"));
		}
		return targetPath;
	}

}
