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
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.net.PortProber.findFreePort;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test using wdm server.
 *
 * @author Boni Garcia
 * @since 3.0.0
 */
class ServerResolverTest {

    static final Logger log = getLogger(lookup().lookupClass());

    static final String EXT = IS_OS_WINDOWS ? ".exe" : "";

    static int serverPort;

    @BeforeAll
    static void startServer() throws IOException {
        serverPort = findFreePort();
        log.debug("Test is starting WebDriverManager server at port {}",
                serverPort);

        WebDriverManager
                .main(new String[] { "server", String.valueOf(serverPort) });
    }

    @ParameterizedTest
    @MethodSource("data")
    void testServerResolver(String path, String driver) throws IOException {
        String serverUrl = String.format("http://localhost:%s/%s", serverPort,
                path);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequestBase request = new HttpGet(serverUrl);

            // Assert response
            log.debug("Request: GET {}", serverUrl);
            try (CloseableHttpResponse response = client.execute(request)) {
                int responseCode = response.getCode();
                log.debug("Response: {}", responseCode);

                assertThat(responseCode).isEqualTo(200);

                // Assert attachment
                String attachment = String.format("attachment; filename=\"%s\"",
                        driver);
                List<Header> headerList = Arrays.asList(response.getHeaders());
                List<Header> collect = headerList.stream().filter(
                        x -> x.toString().contains("Content-Disposition"))
                        .collect(toList());

                log.debug("Assessing {} ... headers should contain {}", driver,
                        attachment);
                assertThat(collect.get(0)).toString().contains(attachment);
            }
        }
    }

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("chromedriver", "chromedriver" + EXT),
                Arguments.of("firefoxdriver", "geckodriver" + EXT),
                Arguments.of("operadriver", "operadriver" + EXT),
                Arguments.of("edgedriver", "msedgedriver" + EXT),
                Arguments.of("chromedriver?os=WIN", "chromedriver.exe"),
                Arguments.of(
                        "chromedriver?os=LINUX&chromeDriverVersion=2.41&forceCache=true",
                        "chromedriver"));
    }

}
