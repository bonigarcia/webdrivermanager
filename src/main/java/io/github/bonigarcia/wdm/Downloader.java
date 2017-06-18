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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
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

    private final BrowserManager browserManager;
    private final WdmHttpClient httpClient;

    private boolean override = WdmConfig.getBoolean("wdm.override");

    public Downloader(BrowserManager browserManager) {
        this(browserManager, new WdmHttpClient.Builder().build());
    }

    /**
     * @since 1.6.2
     */
    public Downloader(BrowserManager browserManager, WdmHttpClient httpClient) {
        this.browserManager = browserManager;
        this.httpClient = httpClient;
    }

    public synchronized void download(URL url, String version, String export,
            List<String> driverName) throws IOException {
        File targetFile = new File(getTarget(version, url));
        File binary = null;

        // Check if binary exists
        boolean download = !targetFile.getParentFile().exists()
                || (targetFile.getParentFile().exists()
                        && targetFile.getParentFile().list().length == 0)
                || override;

        if (!download) {
            // Check if existing binary is valid
            Collection<File> listFiles = FileUtils
                    .listFiles(targetFile.getParentFile(), null, true);
            for (File file : listFiles) {
                for (String s : driverName) {
                    if (file.getName().startsWith(s) && file.canExecute()) {
                        binary = file;
                        log.debug(
                                "Using binary driver previously downloaded {}",
                                binary);
                        download = false;
                        break;
                    } else {
                        download = true;
                    }
                }
                if (!download) {
                    break;
                }
            }
        }

        if (download) {
            log.info("Downloading {} to {}", url, targetFile);
            WdmHttpClient.Get get = new WdmHttpClient.Get(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("Connection", "keep-alive");

            FileUtils.copyInputStreamToFile(
                    httpClient.execute(get).getContent(), targetFile);

            if (!export.contains("edge")) {
                binary = extract(targetFile, export);
            } else {
                binary = targetFile;
            }

            if (targetFile.getName().toLowerCase().endsWith(".msi")) {
                binary = extractMsi(targetFile);
            }

        }
        if (export != null) {
            browserManager.exportDriver(export, binary.toString());
        }
    }

    public File extract(File compressedFile, String export) throws IOException {
        log.trace("Compressed file {}", compressedFile);

        File file = null;
        if (compressedFile.getName().toLowerCase().endsWith("tar.bz2")) {
            file = unBZip2(compressedFile);
        } else if (compressedFile.getName().toLowerCase().endsWith("tar.gz")) {
            file = unTarGz(compressedFile);
        } else if (compressedFile.getName().toLowerCase().endsWith("gz")) {
            file = unGzip(compressedFile);
        } else {
            file = unZip(compressedFile);
        }

        compressedFile.delete();
        file = browserManager.postDownload(compressedFile);

        File result = file.getAbsoluteFile();
        result.setExecutable(true);
        log.trace("Resulting binary file {}", result);

        return result;
    }

    public File unZip(File compressedFile) throws IOException {
        File file = null;
        ZipFile zipFolder = new ZipFile(compressedFile);
        Enumeration<?> enu = zipFolder.entries();

        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            log.trace("Unzipping {} (size: {} KB, compressed size: {} KB)",
                    name, size, compressedSize);

            file = new File(
                    compressedFile.getParentFile() + File.separator + name);
            if (!file.exists() || override) {
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

        return file;
    }

    public File unGzip(File archive) throws IOException {
        log.trace("UnGzip {}", archive);
        String fileName = archive.getName();
        int iDash = fileName.indexOf("-");
        if (iDash != -1) {
            fileName = fileName.substring(0, iDash);
        }
        int iDot = fileName.indexOf(".");
        if (iDot != -1) {
            fileName = fileName.substring(0, iDot);
        }
        File target = new File(
                archive.getParentFile() + File.separator + fileName);

        try (GZIPInputStream in = new GZIPInputStream(
                new FileInputStream(archive))) {
            try (FileOutputStream out = new FileOutputStream(target)) {
                for (int c = in.read(); c != -1; c = in.read()) {
                    out.write(c);
                }
            }
        }

        if (!target.getName().toLowerCase().contains(".exe")
                && target.exists()) {
            target.setExecutable(true);
        }

        return target;
    }

    public File unTarGz(File archive) throws IOException {
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR,
                CompressionType.GZIP);
        archiver.extract(archive, archive.getParentFile());
        log.trace("unTarGz {}", archive);

        return archive;
    }

    public File unBZip2(File archive) throws IOException {
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR,
                CompressionType.BZIP2);
        archiver.extract(archive, archive.getParentFile());
        log.trace("Unbzip2 {}", archive);

        return archive;
    }

    public String getTarget(String version, URL url) throws IOException {
        log.trace("getTarget {} {}", version, url);

        String zip = url.getFile().substring(url.getFile().lastIndexOf("/"));

        int iFirst = zip.indexOf("_");
        int iSecond = zip.indexOf("-");
        int iLast = iFirst != zip.lastIndexOf("_") ? zip.lastIndexOf("_")
                : iSecond != -1 ? iSecond : zip.length();
        String folder = zip.substring(0, iLast).replace(".zip", "")
                .replace(".tar.bz2", "").replace(".tar.gz", "")
                .replace(".msi", "").replace(".exe", "")
                .replace("_", File.separator);

        String target = browserManager.preDownload(
                getTargetPath() + folder + File.separator + version + zip,
                version);
        log.trace("Target file for URL {} version {} = {}", url, version,
                target);

        return target;
    }

    public String getTargetPath() {
        String targetPath = WdmConfig.getString("wdm.targetPath");
        if (targetPath.contains(HOME)) {
            targetPath = targetPath.replace(HOME,
                    System.getProperty("user.home"));
        }

        // Create repository folder if not exits
        File repository = new File(targetPath);
        if (!repository.exists()) {
            repository.mkdirs();
        }
        return targetPath;
    }

    public void forceDownload() {
        this.override = true;
    }

    public File extractMsi(File msi) throws IOException {
        File tmpMsi = new File(Files.createTempDirectory(msi.getName()).toFile()
                .getAbsoluteFile() + File.separator + msi.getName());
        Files.move(msi.toPath(), tmpMsi.toPath());
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
        msi.delete();

        Collection<File> listFiles = FileUtils.listFiles(
                new File(msi.getParent()), new String[] { "exe" }, true);
        return listFiles.iterator().next();
    }

}
