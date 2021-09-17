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
import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

/**
 * Supported architecture enumeration (32/64 bits).
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public enum Architecture {
    DEFAULT(emptyList()), X32(asList("i686", "x86")), X64(emptyList()),
    ARM64(asList("aarch64", "m1"));

    List<String> archLabels;

    Architecture(List<String> archLabels) {
        this.archLabels = archLabels;
    }

    public Stream<String> archLabelsStream() {
        return this.archLabels.stream();
    }

    public boolean matchString(String strMatch) {
        return archLabelsStream().anyMatch(strMatch::contains);
    }

    public boolean matchUrl(URL url) {
        return archLabelsStream().anyMatch(x -> url.getFile().contains(x))
                || url.getFile().contains(this.toString().toLowerCase(ROOT));
    }

    public <T> List<T> filterArm64(List<T> input) {
        if (this != ARM64) {
            return input.stream()
                    .filter(x -> !x.toString().toLowerCase(ROOT)
                            .contains(ARM64.toString().toLowerCase(ROOT))
                            && !ARM64.matchString(x.toString()))
                    .collect(toList());
        }
        return input;
    }

    @Override
    public String toString() {
        return this.name().contains("X") ? this.name().replace("X", "")
                : this.name();
    }

}
