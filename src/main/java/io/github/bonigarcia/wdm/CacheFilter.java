/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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

import static java.io.File.separator;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.sort;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

/**
 * Logic for filtering driver cache.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.0.0
 */
public class CacheFilter {

    final Logger log = getLogger(lookup().lookupClass());

    private String cachePath;

    public CacheFilter(String cachePath) {
        this.cachePath = cachePath;
    }

    public List<File> filterCacheBy(List<File> input, String key,
            boolean isVersion) {
        String pathSeparator = isVersion ? separator : "";
        List<File> output = new ArrayList<>(input);
        if (!key.isEmpty() && !input.isEmpty()) {
            String keyInLowerCase = key.toLowerCase();
            for (File f : input) {
                if (!f.toString().toLowerCase()
                        .contains(pathSeparator + keyInLowerCase)) {
                    output.remove(f);
                }
            }
        }
        log.trace("Filter cache by {} -- input list {} -- output list {} ", key,
                input, output);
        return output;
    }

    public List<File> getFilesInCache() {
        List<File> listFiles = (List<File>) listFiles(new File(cachePath), null,
                true);
        sort(listFiles);
        return listFiles;
    }

}
