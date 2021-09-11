/*
 * (C) Copyright 2015 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.online;

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static java.io.File.separator;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Downloader class.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class Downloader {

    final Logger log = getLogger(lookup().lookupClass());

    HttpClient httpClient;
    Config config;
    Function<File, List<File>> postDownloadFunction;

    public Downloader(HttpClient httpClient, Config config,
            Function<File, List<File>> postDownloadFunction) {
        this.httpClient = httpClient;
        this.config = config;
        this.postDownloadFunction = postDownloadFunction;

    }

    public synchronized String download(URL url, String driverVersion,
            String driverName, DriverManagerType driverManagerType)
            throws IOException {
        File targetFile = getTarget(driverVersion, driverName,
                driverManagerType, url);
        Optional<File> driver = checkDriver(driverName, targetFile);
        if (!driver.isPresent()) {
            driver = downloadAndExtract(url, targetFile);
        }
        return driver.get().toString();
    }

    public File getTarget(String driverVersion, String driverName,
            DriverManagerType driverManagerType, URL url) {
        String zip = url.getFile().substring(url.getFile().lastIndexOf('/'));
        String cachePath = config.getCacheFolder().getAbsolutePath();
        OperatingSystem os = config.getOperatingSystem();
        String architecture = config.getArchitecture().toString()
                .toLowerCase(ROOT);

        if (os.isWin() && (driverManagerType == CHROME
                || driverManagerType == CHROMIUM)) {
            log.trace(
                    "{} in Windows is only available for 32 bits architecture",
                    driverName);
            architecture = "32";
        }

        String osName = os.getName();
        if (config.getArchitecture() == ARM64) {
            osName += "-";
        }
        String target = config.isAvoidOutputTree() ? cachePath + zip
                : cachePath + separator + driverName + separator + osName
                        + architecture + separator + driverVersion + zip;

        log.trace("Target file for URL {} driver version {} = {}", url,
                driverVersion, target);

        return new File(target);
    }

    private Optional<File> downloadAndExtract(URL url, File targetFile)
            throws IOException {
        log.info("Downloading {}", url);
        File targetFolder = targetFile.getParentFile();
        File tempDir = config.isAvoidTmpFolder() ? targetFolder
                : createTempDirectory("").toFile();
        File temporaryFile = new File(tempDir, targetFile.getName());

        log.trace("Target folder {} ... using temporal file {}", targetFolder,
                temporaryFile);
        copyInputStreamToFile(httpClient.execute(httpClient.createHttpGet(url))
                .getEntity().getContent(), temporaryFile);

        List<File> extractedFiles = extract(temporaryFile);
        File resultingDriver = new File(targetFolder,
                extractedFiles.iterator().next().getName());
        boolean driverExists = resultingDriver.exists();

        if ((!driverExists || config.isForceDownload())
                && !config.isAvoidTmpFolder()) {
            if (driverExists) {
                log.debug("Overriding former driver {}", resultingDriver);
                deleteFile(resultingDriver);
            }
            for (File f : extractedFiles) {
                moveFileToDirectory(f, targetFolder, true);
            }
        }
        if (!config.isExecutable(resultingDriver)) {
            setFileExecutable(resultingDriver);
        }
        if (!config.isAvoidTmpFolder()) {
            deleteFolder(tempDir);
        }
        log.trace("Driver after extraction {}", resultingDriver);

        return of(resultingDriver);
    }

    private Optional<File> checkDriver(String driverName, File targetFile) {
        File parentFolder = targetFile.getParentFile();
        if (parentFolder.exists() && !config.isForceDownload()) {
            // Check if driver exits in parent folder and it is valid

            Collection<File> listFiles = listFiles(parentFolder, null, true);
            for (File file : listFiles) {
                if (file.getName().startsWith(driverName)
                        && config.isExecutable(file)) {
                    log.trace("Using {} previously downloaded", driverName);
                    return of(file);
                }
            }
            log.trace("{} does not exist in cache", driverName);
        }
        return empty();
    }

    private List<File> extract(File compressedFile) throws IOException {
        String fileName = compressedFile.getName().toLowerCase(ROOT);

        boolean extractFile = !fileName.endsWith("exe")
                && !fileName.endsWith("jar");
        if (extractFile) {
            log.info("Extracting driver from compressed file {}", fileName);
        }
        if (fileName.endsWith("tar.gz")) {
            unTarGz(compressedFile);
        } else if (fileName.endsWith("gz")) {
            unGzip(compressedFile);
        } else if (fileName.endsWith("zip")) {
            unZip(compressedFile);
        }

        if (extractFile) {
            deleteFile(compressedFile);
        }

        return postDownloadFunction.apply(compressedFile);
    }

    private void unZip(File compressedFile) throws IOException {
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

                file = new File(compressedFile.getParentFile(), name);
                if (!file.exists() || config.isForceDownload()) {
                    if (name.endsWith("/")) {
                        file.mkdirs();
                        continue;
                    }

                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    try (InputStream is = zipFolder.getInputStream(zipEntry)) {
                        copyInputStreamToFile(is, file);
                    }
                    setFileExecutable(file);
                } else {
                    log.debug("{} already exists", file);
                }

            }
        }
    }

    private void unGzip(File archive) throws IOException {
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
        File target = new File(archive.getParentFile(), fileName);

        try (GZIPInputStream in = new GZIPInputStream(
                new FileInputStream(archive))) {
            try (FileOutputStream out = new FileOutputStream(target)) {
                for (int c = in.read(); c != -1; c = in.read()) {
                    out.write(c);
                }
            }
        }

        if (!target.getName().toLowerCase(ROOT).contains(".exe")
                && target.exists()) {
            setFileExecutable(target);
        }
    }

    public void unTarGz(File tarGzFile) throws IOException {
        String destDir = tarGzFile.getParent();
        TarArchiveEntry entry = null;
        TarArchiveEntry[] subEntries = null;
        File subEntryFile = null;
        try (FileInputStream fis = new FileInputStream(tarGzFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                TarArchiveInputStream taris = new TarArchiveInputStream(gis);) {
            while ((entry = taris.getNextTarEntry()) != null) {
                StringBuilder entryFileName = new StringBuilder();
                entryFileName.append(destDir).append(File.separator)
                        .append(entry.getName());
                File entryFile = new File(entryFileName.toString());
                if (entry.isDirectory()) {
                    if (!entryFile.exists()) {
                        entryFile.mkdir();
                    }
                    subEntries = entry.getDirectoryEntries();
                    for (int i = 0; i < subEntries.length; i++) {
                        try (OutputStream out = new FileOutputStream(
                                subEntryFile)) {
                            subEntryFile = new File(entryFileName.toString(),
                                    subEntries[i].getName());
                            IOUtils.copy(taris, out);
                        }
                    }
                } else {
                    try (OutputStream out = new FileOutputStream(entryFile)) {
                        IOUtils.copy(taris, out);
                        if (entryFile.getName().endsWith(".gz")) {
                            unTarGz(entryFile.getAbsoluteFile());
                        }
                    }
                }
            }
        }
    }

    protected void setFileExecutable(File file) {
        if (!file.setExecutable(true)) {
            log.warn("Error setting file {} as executable", file);
        }
    }

    public void renameFile(File from, File to) {
        log.trace("Renaming file from {} to {}", from, to);
        if (to.exists()) {
            deleteFile(to);
        }
        if (!from.renameTo(to)) {
            log.warn("Error renaming file from {} to {}", from, to);
        }
    }

    protected void deleteFile(File file) {
        log.trace("Deleting file {}", file);
        try {
            delete(file.toPath());
        } catch (IOException e) {
            throw new WebDriverManagerException(e);
        }
    }

    public void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            log.trace("Deleting folder {}", folder);
            try {
                deleteDirectory(folder);
            } catch (IOException e) {
                throw new WebDriverManagerException(e);
            }
        } else {
            log.trace("{} cannot be deleted since it is not a directory",
                    folder);
        }
    }

}
