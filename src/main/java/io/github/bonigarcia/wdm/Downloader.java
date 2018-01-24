/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm;

import org.rauschig.jarchivelib.Archiver;
import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.github.bonigarcia.wdm.WdmConfig.getBoolean;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.io.File.separator;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.Files.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.*;
import static org.rauschig.jarchivelib.ArchiveFormat.TAR;
import static org.rauschig.jarchivelib.ArchiverFactory.createArchiver;
import static org.rauschig.jarchivelib.CompressionType.BZIP2;
import static org.rauschig.jarchivelib.CompressionType.GZIP;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Downloader class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class Downloader {

    final Logger log = getLogger(lookup().lookupClass());

    private static final String HOME = "~";

    private BrowserManager browserManager;
    private WdmHttpClient httpClient;
    private boolean isForcingDownload;
    private boolean isBinary;

    public Downloader() {
        this.httpClient = new WdmHttpClient.Builder().build();
    }

    public Downloader(BrowserManager browserManager) {
        this();
        this.browserManager = browserManager;
    }

    public Downloader(BrowserManager browserManager, WdmHttpClient httpClient) {
        this.browserManager = browserManager;
        this.httpClient = httpClient;
    }

    public synchronized void download(URL url, String version, String export,
                                      List<String> driverName) throws IOException, InterruptedException {
        File targetFile = new File(getTarget(version, url));

        boolean download = !targetFile.getParentFile().exists()
                || (targetFile.getParentFile().exists()
                && targetFile.getParentFile().list().length == 0)
                || isForcingDownload || getBoolean("wdm.override");
        Optional<File> binary = (download) ? download(url, targetFile, export)
                : checkBinary(driverName, targetFile);
        if (export != null && binary.isPresent()) {
            browserManager.exportDriver(export, binary.get().toString());
        }
    }

    private Optional<File> download(URL url, File targetFile, String export)
            throws IOException, InterruptedException {
        log.debug("Downloading {} to {}", url, targetFile);
        File temporaryFile = new File(targetFile.getParentFile(),
                randomUUID().toString());
        WdmHttpClient.Get get = new WdmHttpClient.Get(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Connection", "keep-alive");

        copyInputStreamToFile(httpClient.execute(get).getContent(),
                temporaryFile);
        renameFile(temporaryFile, targetFile);

        if (!export.contains("edge") && !isBinary) {
            return of(extract(targetFile));
        } else if (targetFile.getName().toLowerCase().endsWith(".msi")) {
            return of(extractMsi(targetFile));
        } else {
            return of(targetFile);
        }
    }

    private Optional<File> checkBinary(List<String> driverName,
                                       File targetFile) {
        // Check if existing binary is valid
        Collection<File> listFiles = listFiles(targetFile.getParentFile(), null,
                true);
        for (File file : listFiles) {
            for (String s : driverName) {
                if (file.getName().startsWith(s) && file.canExecute()) {
                    log.debug("Using binary driver previously downloaded {}",
                            file);
                    return of(file);
                }
            }
        }
        return empty();
    }

    public File extract(File compressedFile) throws IOException {
        log.trace("Compressed file {}", compressedFile);

        if (compressedFile.getName().toLowerCase().endsWith("tar.bz2")) {
            unBZip2(compressedFile);
        } else if (compressedFile.getName().toLowerCase().endsWith("tar.gz")) {
            unTarGz(compressedFile);
        } else if (compressedFile.getName().toLowerCase().endsWith("gz")) {
            unGzip(compressedFile);
        } else {
            unZip(compressedFile);
        }
        deleteFile(compressedFile);

        File result = browserManager.postDownload(compressedFile)
                .getAbsoluteFile();
        setFileExecutable(result);
        log.trace("Resulting binary file {}", result);

        return result;
    }

    public File unZip(File compressedFile) throws IOException {
        File file = null;
        try (ZipFile zipFolder = new ZipFile(compressedFile)) {
            Enumeration<?> enu = zipFolder.entries();

            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
                log.trace("Unzipping {} (size: {} KB, compressed size: {} KB)",
                        name, size, compressedSize);

                file = new File(
                        compressedFile.getParentFile() + separator + name);
                if (!file.exists() || isForcingDownload
                        || getBoolean("wdm.override")) {
                    if (name.endsWith("/")) {
                        file.mkdirs();
                        continue;
                    }

                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    try (InputStream is = zipFolder.getInputStream(zipEntry)) {
                        File temporaryFile = new File(parent,
                                randomUUID().toString());
                        copyInputStreamToFile(is, temporaryFile);
                        renameFile(temporaryFile, file);
                    }
                    setFileExecutable(file);
                } else {
                    log.debug("{} already exists", file);
                }

            }
        }

        return file;
    }

    public File unGzip(File archive) throws IOException {
        log.trace("UnGzip {}", archive);
        String fileName = archive.getName();
        int iDash = fileName.indexOf('-');
        if (iDash != -1) {
            fileName = fileName.substring(0, iDash);
        }
        int iDot = fileName.indexOf('.');
        if (iDot != -1) {
            fileName = fileName.substring(0, iDot);
        }
        File target = new File(archive.getParentFile() + separator + fileName);

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
            setFileExecutable(target);
        }

        return target;
    }

    public File unTarGz(File archive) throws IOException {
        Archiver archiver = createArchiver(TAR, GZIP);
        archiver.extract(archive, archive.getParentFile());
        log.trace("unTarGz {}", archive);

        return archive;
    }

    public File unBZip2(File archive) throws IOException {
        Archiver archiver = createArchiver(TAR, BZIP2);
        archiver.extract(archive, archive.getParentFile());
        log.trace("Unbzip2 {}", archive);

        return archive;
    }

    public String getTarget(String version, URL url) {
        log.trace("getTarget {} {}", version, url);

        String file = url.getFile().substring(url.getFile().lastIndexOf('/'));

        if (file.endsWith(".bin") || file.endsWith(".jar")) {
            final String fileEnding = file.substring(file.lastIndexOf("."), file.length());
            if (browserManager.myOsName.toLowerCase().contains("win")) {
                file = file.replace(fileEnding, ".exe");
            } else {
                file = file.replace(fileEnding, "");
            }
            isBinary = true;
        }

        int iFirst = file.indexOf('_');
        int iSecond = file.indexOf('-');
        int iLast = file.length();
        if (iFirst != file.lastIndexOf('_')) {
            iLast = file.lastIndexOf('_');
        } else if (iSecond != -1) {
            iLast = iSecond;
        }

        String folder = file.substring(0, iLast).replace(".zip", "")
                .replace(".tar.bz2", "").replace(".tar.gz", "")
                .replace(".msi", "").replace(".exe", "")
                .replace(".bin", "").replace(".jar", "")
                .replace("_", separator);


        String target;
        if (!browserManager.isUsingNexus) {
            target = browserManager.preDownload(
                    getTargetPath() + folder + separator + version + file, version);
        } else {
            target = getTargetPath() + folder + separator + version + file;
        }
        log.trace("Target file for URL {} version {} = {}", url, version,
                target);

        return target;
    }

    public String getTargetPath() {
        String targetPath = getString("wdm.targetPath");
        if (targetPath.contains(HOME)) {
            targetPath = targetPath.replace(HOME, getProperty("user.home"));
        }
        log.trace("Target path {}", targetPath);

        // Create repository folder if not exits
        File repository = new File(targetPath);
        if (!repository.exists()) {
            repository.mkdirs();
        }
        return targetPath;
    }

    public void forceDownload() {
        this.isForcingDownload = true;
    }

    public File extractMsi(File msi) throws IOException, InterruptedException {
        File tmpMsi = new File(
                createTempDirectory(msi.getName()).toFile().getAbsoluteFile()
                        + separator + msi.getName());
        move(msi.toPath(), tmpMsi.toPath());
        log.trace("Temporal msi file: {}", tmpMsi);

        Process process = getRuntime().exec(new String[]{"msiexec", "/a",
                tmpMsi.toString(), "/qb", "TARGETDIR=" + msi.getParent()});
        try {
            process.waitFor();
        } finally {
            process.destroy();
        }

        deleteFile(tmpMsi);
        deleteFile(msi);

        Collection<File> listFiles = listFiles(new File(msi.getParent()),
                new String[]{"exe"}, true);
        return listFiles.iterator().next();
    }

    protected void setFileExecutable(File file) {
        log.trace("Setting file {} as executable", file);
        if (!file.setExecutable(true)) {
            log.warn("Error setting file {} as executable", file);
        }
    }

    protected void renameFile(File from, File to) {
        log.trace("Renaming file from {} to {}", from, to);
        if (!from.renameTo(to)) {
            System.out.println("ERROR IN RENAMING");
            log.warn("Error renaming file from {} to {}", from, to);
        }
    }

    protected void deleteFile(File file) throws IOException {
        log.trace("Deleting file {}", file);
        delete(file.toPath());
    }

    protected void deleteFolder(File folder) {
        assert folder.isDirectory();
        log.trace("Deleting folder {}", folder);
        try {
            deleteDirectory(folder);
        } catch (IOException e) {
            throw new WebDriverManagerException(e);
        }
    }

}
