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
package io.github.bonigarcia.wdm.config;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

/**
 * Supported architecture enumeration (32/64 bits).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public enum Architecture {
    DEFAULT(emptyList()), X32(asList("i686", "x86")), X64(emptyList());

    List<String> archLabels;

    Architecture(List<String> archLabels) {
        this.archLabels = archLabels;
    }

    public Stream<String> archLabelsStream() {
        return this.archLabels.stream();
    }

    public boolean matchUrl(URL url) {
        return archLabelsStream().anyMatch(x -> url.getFile().contains(x))
                || url.getFile().contains(this.toString());
    }

    @Override
    public String toString() {
        return this.name().contains("X") ? this.name().replace("X", "")
                : this.name();
    }

}
