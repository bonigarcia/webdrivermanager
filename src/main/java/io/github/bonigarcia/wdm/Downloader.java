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

import static io.github.bonigarcia.wdm.Config.listToString;
import static io.github.bonigarcia.wdm.WebDriverManager.config;
import static java.io.File.separator;
import static java.lang.Runtime.getRuntime;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.move;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;
import static org.rauschig.jarchivelib.ArchiveFormat.TAR;
import static org.rauschig.jarchivelib.ArchiverFactory.createArchiver;
import static org.rauschig.jarchivelib.CompressionType.BZIP2;
import static org.rauschig.jarchivelib.CompressionType.GZIP;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.rauschig.jarchivelib.Archiver;
import org.slf4j.Logger;

/**
 * Downloader class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class Downloader {

    final Logger log = getLogger(lookup().lookupClass());

    DriverManagerType driverManagerType;
    HttpClient httpClient;

    public Downloader(DriverManagerType driverManagerType) {
        this.driverManagerType = driverManagerType;
        httpClient = WebDriverManager.getInstance(driverManagerType)
                .getHttpClient();
    }

    public synchronized Optional<String> download(URL url, String version,
            String export, List<String> driverName)
            throws IOException, InterruptedException {
        File targetFile = new File(getTarget(version, url));

        File parentFolder = targetFile.getParentFile();
        String[] list = parentFolder.list();
        Optional<File> optionalBinary = checkBinary(driverName, parentFolder);
        boolean download = !parentFolder.exists() || !optionalBinary.isPresent()
                || config().isOverride();

        log.trace("Downloading {} to {} ({})", url, targetFile, download);
        if (!download && log.isTraceEnabled() && list != null) {
            String content = listToString(Arrays.asList(list));
            log.trace("{} file(s) in target folder ({}): {}", list.length,
                    parentFolder, content);
        }

        Optional<File> binary = (download)
                ? downloadAndExtract(url, targetFile, export)
                : optionalBinary;
        if (export != null && binary.isPresent()) {
            return Optional.of(binary.get().toString());
        }
        return Optional.empty();
    }

    private Optional<File> downloadAndExtract(URL url, File targetFile,
            String export) throws IOException, InterruptedException {
        File targetFolder = targetFile.getParentFile();
        log.info("Downloading {} to folder {}", url, targetFolder);
        File tempDir = createTempDirectory("").toFile();
        File temporaryFile = new File(tempDir, targetFile.getName());
        log.trace("Using temporal file {}", temporaryFile);
        copyInputStreamToFile(httpClient.execute(httpClient.createHttpGet(url))
                .getEntity().getContent(), temporaryFile);

        File extractedFile;
        if (!export.contains("edge")) {
            extractedFile = extract(temporaryFile);
        } else if (targetFile.getName().toLowerCase().endsWith(".msi")) {
            extractedFile = extractMsi(temporaryFile);
        } else {
            extractedFile = temporaryFile;
        }
        File resultingBinary = new File(targetFolder, extractedFile.getName());
        boolean binaryExists = resultingBinary.exists();
        if (!binaryExists || config().isOverride()) {
            if (binaryExists) {
                log.debug("Overriding former binary {}", resultingBinary);
                deleteFile(resultingBinary);
            }
            moveFileToDirectory(extractedFile, targetFolder, true);
        }
        if (!config().isExecutable(resultingBinary)) {
            setFileExecutable(resultingBinary);
        }
        deleteFolder(tempDir);
        log.trace("Binary driver after extraction {}", resultingBinary);

        return Optional.of(resultingBinary);
    }

    private Optional<File> checkBinary(List<String> driverName,
            File parentFolder) {
        // Check if binary exits in parent folder and it is valid
        if (parentFolder.exists()) {
            Collection<File> listFiles = listFiles(parentFolder, null, true);
            for (File file : listFiles) {
                for (String s : driverName) {
                    if (file.getName().startsWith(s)
                            && config().isExecutable(file)) {
                        log.info("Using binary driver previously downloaded");
                        return of(file);
                    }
                }
            }
            log.trace("{} does not exist in cache", driverName);
        }
        return empty();
    }

    public String getTarget(String version, URL url) {
        log.trace("getTarget {} {}", version, url);
        String zip = url.getFile().substring(url.getFile().lastIndexOf('/'));

        int iFirst = zip.indexOf('_');
        int iSecond = zip.indexOf('-');
        int iLast = zip.length();
        if (iFirst != zip.lastIndexOf('_')) {
            iLast = zip.lastIndexOf('_');
        } else if (iSecond != -1) {
            iLast = iSecond;
        }

        String folder = zip.substring(0, iLast).replace(".zip", "")
                .replace(".tar.bz2", "").replace(".tar.gz", "")
                .replace(".msi", "").replace(".exe", "")
                .replace("_", separator);
        String path = config().isAvoidOutputTree() ? getTargetPath() + zip
                : getTargetPath() + folder + separator + version + zip;
        String target = WebDriverManager.getInstance(driverManagerType)
                .preDownload(path, version);

        log.trace("Target file for URL {} version {} = {}", url, version,
                target);

        return new File(target).getPath();
    }

    public String getTargetPath() {
        String targetPath = config().getTargetPath();
        log.trace("Target path {}", targetPath);

        // Create repository folder if not exits
        File repository = new File(targetPath);
        if (!repository.exists()) {
            repository.mkdirs();
        }
        return targetPath;
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

        File result = WebDriverManager.getInstance(driverManagerType)
                .postDownload(compressedFile).getAbsoluteFile();
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

                file = new File(compressedFile.getParentFile(), name);
                if (!file.exists() || config().isOverride()) {
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
        File target = new File(archive.getParentFile(), fileName);

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

    public File extractMsi(File msi) throws IOException, InterruptedException {
        File tmpMsi = new File(
                createTempDirectory(msi.getName()).toFile().getAbsoluteFile()
                        + separator + msi.getName());
        move(msi.toPath(), tmpMsi.toPath());
        log.trace("Temporal msi file: {}", tmpMsi);

        Process process = getRuntime().exec(new String[] { "msiexec", "/a",
                tmpMsi.toString(), "/qb", "TARGETDIR=" + msi.getParent() });
        try {
            process.waitFor();
        } finally {
            process.destroy();
        }

        deleteFile(tmpMsi);
        deleteFile(msi);

        Collection<File> listFiles = listFiles(new File(msi.getParent()),
                new String[] { "exe" }, true);
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
