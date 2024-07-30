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

import static java.lang.String.join;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

/**
 * Command line executor.
 *
 * @author Boni Garcia
 * @since 3.0.0
 */
public class Shell {

    static final Logger log = getLogger(lookup().lookupClass());

    private Shell() {
        throw new IllegalStateException("Utility class");
    }

    public static String runAndWait(String... command) {
        return runAndWait(true, command);
    }

    public static String runAndWait(File folder, String... command) {
        return runAndWaitArray(true, folder, command);
    }

    public static String runAndWait(boolean logCommand, String... command) {
        return runAndWaitArray(logCommand, new File("."), command);
    }

    public static String runAndWait(boolean logCommand, File folder,
            String... command) {
        return runAndWaitArray(logCommand, folder, command);
    }

    public static String runAndWaitArray(boolean logCommand, File folder,
            String[] command) {
        String commandStr = Arrays.toString(command);
        if (logCommand) {
            log.debug("Running command on the shell: {}", commandStr);
        }
        String result = runAndWaitNoLog(folder, command);
        if (logCommand) {
            log.debug("Result: {}", result);
        }
        return result;
    }

    public static String runAndWaitNoLog(File folder, String... command) {
        String output = "";
        try {
            Process process = new ProcessBuilder(command).directory(folder)
                    .redirectErrorStream(false).start();
            process.waitFor();
            output = IOUtils.toString(process.getInputStream(), UTF_8);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "There was a problem executing command <{}> on the shell: {}",
                        join(" ", command), e.getMessage());
            }
        }
        return output.trim();
    }

}
