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
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.WebDriverManagerException;

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

    public static String runAndWait(int timeoutSeconds, String... command) {
        return runAndWait(timeoutSeconds, true, command);
    }

    public static String runAndWait(int timeoutSeconds, File folder,
            String... command) {
        return runAndWaitArray(timeoutSeconds, true, folder, command);
    }

    public static String runAndWait(int timeoutSeconds, boolean logCommand,
            String... command) {
        return runAndWaitArray(timeoutSeconds, logCommand, new File("."),
                command);
    }

    public static String runAndWait(int timeoutSeconds, boolean logCommand,
            File folder, String... command) {
        return runAndWaitArray(timeoutSeconds, logCommand, folder, command);
    }

    public static String runAndWaitArray(int timeoutSeconds, boolean logCommand,
            File folder, String[] command) {
        String commandStr = Arrays.toString(command);
        if (logCommand) {
            log.debug("Running command on the shell: {}", commandStr);
        }
        String result = runAndWaitNoLog(timeoutSeconds, folder, command);
        if (logCommand) {
            log.trace("Result: {}", result);
        }
        return result;
    }

    public static String runAndWaitNoLog(int timeoutSeconds, File folder,
            String... command) {
        try {
            Process process = new ProcessBuilder(command).directory(folder)
                    .redirectErrorStream(false).start();
            StreamReader.consumeStderr(process);
            StreamReader stdout = StreamReader.consumeStdout(process);
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new WebDriverManagerException(
                        "Command timed out: " + String.join(" ", command));
            }
            return stdout.getOutput().trim();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "There was a problem executing command <{}> on the shell: {}",
                        join(" ", command), e.getMessage());
            }
        }
        return StringUtils.EMPTY;
    }

    private static class StreamReader implements Runnable {
        private final CompletableFuture<String> output = new CompletableFuture<>();
        private final InputStream is;
        private static final AtomicInteger id = new AtomicInteger(0);

        private StreamReader(InputStream is) {
            this.is = is;
        }

        private static StreamReader consume(InputStream is, String streamName) {
            StreamReader streamReader = new StreamReader(is);
            Thread t = new Thread(streamReader,
                    "streamReader-" + id.getAndIncrement() + "-" + streamName);
            t.setDaemon(true);
            t.start();
            return streamReader;
        }

        public static StreamReader consumeStdout(Process process) {
            return consume(process.getInputStream(), "stdout");
        }

        public static StreamReader consumeStderr(Process process) {
            return consume(process.getErrorStream(), "stderr");
        }

        public void run() {
            try {
                output.complete(IOUtils.toString(is, UTF_8));
            } catch (Exception e) {
                output.completeExceptionally(e);
            }
        }

        public String getOutput()
                throws InterruptedException, ExecutionException {
            return output.get();
        }
    }
}
