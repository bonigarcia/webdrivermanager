/*
 * (C) Copyright 2025 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.webdriver;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Recording naming shared logic.
 *
 * @author Boni Garcia
 * @since 6.1.0
 */
public class Recording {

    public static final String DOCKER_RECORDING_EXT = ".mp4";
    public static final String EXTENSION_RECORDING_EXT = ".webm";
    private static final String SEPARATOR = "_";
    private static final String DATE_FORMAT = "yyyy.MM.dd_HH.mm.ss.SSS";

    public static String getRecordingName(String browserName,
            String sessionId) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return browserName + SEPARATOR + dateFormat.format(now) + SEPARATOR
                + sessionId;
    }

    public static String getRecordingNameForDocker(String browserName,
            String sessionId) {
        return getRecordingName(browserName, sessionId) + DOCKER_RECORDING_EXT;
    }

}
