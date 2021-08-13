/*
 * (C) Copyright 2018 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.versions;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Comparator;

import org.slf4j.Logger;

/**
 * Version comparator.
 *
 * @author Boni Garcia
 * @since 2.1.0
 */
public class VersionComparator implements Comparator<String> {

    final Logger log = getLogger(lookup().lookupClass());

    @Override
    public int compare(String v1, String v2) {
        String[] v1split = v1.split("\\.");
        String[] v2split = v2.split("\\.");
        int length = max(v1split.length, v2split.length);
        for (int i = 0; i < length; i++) {
            try {
                int v1Part = i < v1split.length ? parseInt(v1split[i]) : 0;
                int v2Part = i < v2split.length ? parseInt(v2split[i]) : 0;
                if (v1Part < v2Part) {
                    return -1;
                }
                if (v1Part > v2Part) {
                    return 1;
                }
            } catch (Exception e) {
                log.trace("Exception comparing {} with {} ({})", v1, v2,
                        e.getMessage());
            }
        }
        return 0;
    }
}
