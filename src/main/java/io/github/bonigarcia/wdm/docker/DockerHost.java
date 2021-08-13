/*
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.docker;

import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static java.util.Locale.ROOT;

import java.net.URI;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It represents a dockerd endpoint (a codified DOCKER_HOST).
 *
 * @author Boni Garcia
 * @since 5.0.0
 */

public class DockerHost {

    private static final String DEFAULT_UNIX_ENDPOINT = "unix:///var/run/docker.sock";
    private static final String DEFAULT_WINDOWS_ENDPOINT = "npipe:////./pipe/docker_engine";
    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 2375;

    private String host;
    private URI uri;
    private URI bindUri;
    private String address;
    private int port;
    private String certPath;
    private String endpoint;

    private DockerHost(String endpoint, String certPath) {
        if (endpoint.startsWith("unix://")) {
            this.port = 0;
            this.address = DEFAULT_ADDRESS;
            this.host = endpoint;
            this.uri = URI.create(endpoint);
            this.bindUri = URI.create(endpoint);
        } else {
            String stripped = endpoint.replaceAll(".*://", "");
            Pattern hostPattern = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");
            Matcher hostMatcher = hostPattern.matcher(stripped);

            String scheme = isNullOrEmpty(certPath) ? "http" : "https";
            this.address = hostMatcher.matches() ? hostMatcher.group(1)
                    : DEFAULT_ADDRESS;
            this.port = hostMatcher.matches()
                    ? Integer.parseInt(hostMatcher.group(2))
                    : defaultPort();
            this.host = address + ":" + port;
            this.uri = URI.create(scheme + "://" + address + ":" + port);
            this.bindUri = URI.create("tcp://" + address + ":" + port);
        }

        this.endpoint = endpoint;
        this.certPath = certPath;
    }

    public String endpoint() {
        return endpoint;
    }

    public String host() {
        return host;
    }

    public URI uri() {
        return uri;
    }

    public URI bindUri() {
        return bindUri;
    }

    public int port() {
        return port;
    }

    public String address() {
        return address;
    }

    public String dockerCertPath() {
        return certPath;
    }

    public static DockerHost fromEnv() {
        String host = endpointFromEnv();
        String certPath = certPathFromEnv();
        return new DockerHost(host, certPath);
    }

    public static DockerHost from(String endpoint, String certPath) {
        return new DockerHost(endpoint, certPath);
    }

    public static String defaultDockerEndpoint() {
        String osName = System.getProperty("os.name");
        String os = osName.toLowerCase(ROOT);
        if (os.equalsIgnoreCase("linux") || os.contains("mac")) {
            return defaultUnixEndpoint();
        } else if (os.contains("windows")) {
            return defaultWindowsEndpoint();
        } else {
            return "http://" + defaultAddress() + ":" + defaultPort();
        }
    }

    public static String endpointFromEnv() {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null) {
            dockerHost = defaultDockerEndpoint();
        }
        return dockerHost;
    }

    public static String defaultUnixEndpoint() {
        return DEFAULT_UNIX_ENDPOINT;
    }

    public static String defaultWindowsEndpoint() {
        return DEFAULT_WINDOWS_ENDPOINT;
    }

    public static String defaultAddress() {
        return DEFAULT_ADDRESS;
    }

    public static int defaultPort() {
        return DEFAULT_PORT;
    }

    public static int portFromEnv() {
        String port = System.getenv("DOCKER_PORT");
        if (port == null) {
            return defaultPort();
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return defaultPort();
        }
    }

    public static String defaultCertPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".docker").toString();
    }

    public static String certPathFromEnv() {
        return System.getenv("DOCKER_CERT_PATH");
    }

    public static String configPathFromEnv() {
        return System.getenv("DOCKER_CONFIG");
    }

}
