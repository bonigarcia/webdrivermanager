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
package io.github.bonigarcia.wdm.test.versions;

import static java.io.File.separator;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.cache.CacheHandler;
import io.github.bonigarcia.wdm.config.Config;

/**
 * FilterCacheBy test.
 *
 * @author Boni Garcia
 * @since 3.8.0
 */
class VersionFilterTest {

    final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @MethodSource("data")
    void testFilterCacheBy(String version, int expectedVersions) {
        CacheHandler cacheHandler = new CacheHandler(new Config());
        List<File> filteredList = cacheHandler.filterCacheBy(getInputFileList(),
                version, true);

        log.debug("Version {} -- Output {}", version, filteredList);
        assertThat(filteredList.size()).isEqualTo(expectedVersions);
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("74", 1), Arguments.of("77", 1),
                Arguments.of("79", 2));
    }

    private List<File> getInputFileList() {
        List<File> output = new ArrayList<>();
        File currentFolder = new File(".");
        String[] versions = { "74.0.3729.6", "75.0.3770.140", "75.0.3770.8",
                "75.0.3770.90", "76.0.3809.126", "76.0.3809.68", "77.0.3865.40",
                "78.0.3904.70", "79.0.3945.16", "79.0.3945.36" };
        for (String v : versions) {
            output.add(new File(currentFolder, v + separator + "chromedriver"));
        }
        return output;
    }

}
