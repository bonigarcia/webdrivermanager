/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

/**
 * Command line executor.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.0
 */
public class Shell {

    static final Logger log = getLogger(lookup().lookupClass());

    private Shell() {
        throw new IllegalStateException("Utility class");
    }

    public static String runAndWait(String... command) {
        return runAndWaitArray(command);
    }

    public static String runAndWaitArray(String[] command) {
        String commandStr = Arrays.toString(command);
        log.trace("Running command on the shell: {}", commandStr);
        String result = runAndWaitNoLog(command);
        log.trace("Result: {}", result);
        return result;
    }

    public static String runAndWaitNoLog(String... command) {
        String output = "";
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true).start();
            output = IOUtils.toString(process.getInputStream(), UTF_8);
            process.destroy();
        } catch (IOException e) {
            log.trace("There was an error executing command {} on the shell",
                    command, e);
        }
        return output.trim();
    }

    public static String getVersionFromWmicOutput(String output) {
        int i = output.indexOf('=');
        int j = output.indexOf('.');
        return i != -1 && j != -1 ? output.substring(i + 1, j) : output;
    }

    public static String getVersionFromPosixOutput(String output,
            String driverType) {
        int i = output.indexOf(driverType);
        int j = output.indexOf('.');
        return i != -1 && j != -1
                ? output.substring(i + driverType.length(), j).trim()
                : output;
    }

    public static String getVersionFromPowerShellOutput(String output) {
        int i = output.indexOf("Version");
        int j = output.indexOf(':', i);
        int k = output.indexOf('.', j);
        return i != -1 && j != -1 && k != -1 ? output.substring(j + 1, k).trim()
                : output;
    }

}
