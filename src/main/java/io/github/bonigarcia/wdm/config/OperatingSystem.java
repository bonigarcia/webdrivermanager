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
package io.github.bonigarcia.wdm.config;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Locale.ROOT;

import java.util.List;
import java.util.stream.Stream;

/**
 * Supported operative system enumeration.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public enum OperatingSystem {
    WIN(emptyList()), LINUX(emptyList()), MAC(asList("osx"));

    List<String> osLabels;

    OperatingSystem(List<String> osLabels) {
        this.osLabels = osLabels;
    }

    public Stream<String> osLabelsStream() {
        return this.osLabels.stream();
    }

    public boolean matchOs(String os) {
        return osLabelsStream().anyMatch(os::contains)
                || os.contains(getName());
    }

    public String getName() {
        return this.name().toLowerCase(ROOT);
    }

    public boolean isWin() {
        return this == WIN;
    }

    public boolean isMac() {
        return this == MAC;
    }

    public boolean isLinux() {
        return this == LINUX;
    }

}
