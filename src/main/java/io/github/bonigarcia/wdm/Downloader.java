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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static void download(URL url, String export) throws IOException {
		File targetFile = new File(getTarget(url));
		File binary;

		if (!targetFile.getParentFile().exists()
				|| Boolean.parseBoolean(Config.getProperty("override"))) {
			log.debug("Downloading " + url + " to " + targetFile);
			FileUtils.copyURLToFile(url, targetFile);

			log.debug("Unziping " + targetFile);
			binary = unZip(targetFile);
			targetFile.delete();
		} else {
			binary = targetFile.getParentFile().listFiles()[0];
			log.debug("Using binary driver previously downloaded {}", binary);
		}

		log.debug("Exporting {} as {}", export, binary.toString());
		System.setProperty(export, binary.toString());

	}

	public static File unZip(String fileInput, String outputFolder)
			throws IOException {
		return null;
	}

	public static File unZip(File folder) throws IOException {
		ZipFile zipFolder = new ZipFile(folder);
		Enumeration<?> enu = zipFolder.entries();
		File file = null;

		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();

			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			log.debug("Unzipping {} (size: {} KB, compressed size: {} KB)",
					name, size, compressedSize);

			file = new File(folder.getParentFile() + File.separator + name);
			if (!file.exists()
					|| Boolean.parseBoolean(Config.getProperty("override"))) {
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
			} else {
				log.debug(file + " already exists");
			}

		}
		zipFolder.close();

		return file.getAbsoluteFile();
	}

	private static String getTarget(URL url) throws IOException {
		String zip = url.getFile().substring(url.getFile().lastIndexOf("/"));
		String folder = zip.replace(".zip", "").replace("_", File.separator);
		String targetPath = Config.getProperty("targetPath");
		if (targetPath.contains(HOME)) {
			targetPath = targetPath.replace(HOME,
					System.getProperty("user.home"));
		}
		return targetPath + folder + File.separator + zip;
	}

}
