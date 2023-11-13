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


    private final VersionUtil versionUtil;


    public VersionComparator(VersionUtil versionUtil) {
        this.versionUtil = versionUtil;
    }


    @Override
    public int compare(String v1, String v2) {
        return versionUtil.compare(v1, v2);
    }
}
