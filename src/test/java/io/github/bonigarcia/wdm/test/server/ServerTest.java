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
package io.github.bonigarcia.wdm.test.server;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Test using wdm server.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.0
 */
class ServerTest {

    static final Logger log = getLogger(lookup().lookupClass());

    static final String EXT = IS_OS_WINDOWS ? ".exe" : "";

    static String serverPort;

    @BeforeAll
    static void startServer() throws IOException {
        serverPort = getFreePort();
        log.debug("Test is starting WebDriverManager server at port {}",
                serverPort);

        WebDriverManager.main(new String[] { "server", serverPort });
    }

    @ParameterizedTest
    @MethodSource("data")
    void testServer(String path, String driver) throws IOException {
        String serverUrl = String.format("http://localhost:%s/%s", serverPort,
                path);

        int timeoutSeconds = 60;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, SECONDS)
                .readTimeout(timeoutSeconds, SECONDS)
                .callTimeout(timeoutSeconds, SECONDS).build();

        Request request = new Request.Builder().url(serverUrl).build();

        // Assert response
        log.debug("Request: GET {}", serverUrl);
        Response response = client.newCall(request).execute();
        log.debug("Response: {}", response.code());

        assertThat(response.isSuccessful()).isTrue();

        // Assert attachment
        String attachment = String.format("attachment; filename=\"%s\"",
                driver);

        List<String> headers = response.headers().values("Content-Disposition");
        log.debug("Assessing {} ... {} should contain {}", driver, headers,
                attachment);
        assertThat(headers).contains(attachment);
    }

    static String getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return String.valueOf(socket.getLocalPort());
        }
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("chromedriver", "chromedriver" + EXT),
                Arguments.of("firefoxdriver", "geckodriver" + EXT),
                Arguments.of("operadriver", "operadriver" + EXT),
                Arguments.of("edgedriver", "msedgedriver" + EXT),
                Arguments.of("iedriver", "IEDriverServer.exe"),
                Arguments.of("chromedriver?os=WIN", "chromedriver.exe"),
                Arguments.of(
                        "chromedriver?os=LINUX&chromeDriverVersion=2.41&forceCache=true",
                        "chromedriver"));
    }

}
