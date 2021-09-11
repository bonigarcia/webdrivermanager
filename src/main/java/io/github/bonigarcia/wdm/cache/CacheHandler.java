/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.cache;

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static java.io.File.separator;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.sort;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Logic for filtering driver cache.
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
public class CacheHandler {

    final Logger log = getLogger(lookup().lookupClass());

    private Config config;

    public CacheHandler(Config config) {
        this.config = config;
    }

    public List<File> filterCacheBy(List<File> input, String key,
            boolean isVersion) {
        String pathSeparator = isVersion ? separator : "";
        List<File> output = new ArrayList<>(input);

        if (!key.isEmpty() && !input.isEmpty()) {
            output = input.stream()
                    .filter(file -> file.toString().toLowerCase(ROOT)
                            .contains(pathSeparator + key.toLowerCase(ROOT)))
                    .collect(toList());
        }
        log.trace("Filter cache by {} -- input list {} -- output list {} ", key,
                input, output);
        return output;
    }

    public List<File> getFilesInCache() {
        List<File> listFiles = (List<File>) listFiles(config.getCacheFolder(),
                null, true);
        sort(listFiles);
        return listFiles;
    }

    public Optional<String> getDriverFromCache(String driverVersion,
            String driverName, DriverManagerType driverManagerType,
            Architecture arch, String os) {
        log.trace("Checking if {} exists in cache", driverName);
        List<File> filesInCache = getFilesInCache();
        if (!filesInCache.isEmpty()) {
            // Filter by name
            filesInCache = filterCacheBy(filesInCache, driverName, false);

            // Filter by version
            filesInCache = filterCacheBy(filesInCache, driverVersion, true);

            // Filter by OS
            filesInCache = filterCacheBy(filesInCache, os, false);

            // Filter by ARM64 architecture
            filesInCache = config.getArchitecture().filterArm64(filesInCache);

            if (filesInCache.size() == 1 && config.getArchitecture() != ARM64) {
                return Optional.of(filesInCache.get(0).toString());
            }

            // Filter by arch
            if (os.equalsIgnoreCase("win") && (driverManagerType == CHROME
                    || driverManagerType == CHROMIUM)) {
                log.trace(
                        "Avoid filtering for architecture {} with {} in Windows",
                        arch, driverName);
            } else {
                filesInCache = filterCacheBy(filesInCache, arch.toString(),
                        false);
            }

            if (!filesInCache.isEmpty()) {
                return Optional.of(
                        filesInCache.get(filesInCache.size() - 1).toString());
            }
        }

        log.trace("{} not found in cache", driverName);
        return Optional.empty();
    }

}
