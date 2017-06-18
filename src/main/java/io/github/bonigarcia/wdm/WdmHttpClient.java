/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.WdmUtils.isNullOrEmpty;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Http Client for WebDriverManager.
 *
 * @author Kazuki Shimizu
 * @since 1.6.2
 */
public class WdmHttpClient implements Closeable {

    protected static final Logger log = LoggerFactory
            .getLogger(BrowserManager.class);

    private final CloseableHttpClient httpClient;

    private WdmHttpClient(String proxyUrl, String proxyUser, String proxyPass) {
        HttpHost proxyHost = createProxyHttpHost(proxyUrl);
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManagerShared(true);
        if (proxyHost != null) {
            builder.setProxy(proxyHost);
            BasicCredentialsProvider credentialsProvider = createBasicCredentialsProvider(
                    proxyUrl, proxyUser, proxyPass, proxyHost);
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        this.httpClient = builder.build();
    }

    Proxy createProxy(String proxyUrl) {
        URL url = determineProxyUrl(proxyUrl);
        if (url == null) {
            return null;
        }
        String proxyHost = url.getHost();
        int proxyPort = url.getPort() == -1 ? 80 : url.getPort();
        return new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
    }

    public Response execute(Method method) throws IOException {
        HttpResponse response = httpClient.execute(method.toHttpUriRequest());
        if (response.getStatusLine()
                .getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
            throw new RuntimeException("A response error is detected. "
                    + response.getStatusLine());
        }
        return new Response(response);
    }

    public boolean isValid(URL url) throws IOException {
        HttpResponse response = httpClient
                .execute(new WdmHttpClient.Options(url).toHttpUriRequest());
        if (response.getStatusLine()
                .getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
            log.debug("A response error is detected. {}",
                    response.getStatusLine());
            return false;
        }
        return true;
    }

    private URL determineProxyUrl(String proxy) {
        String proxyInput = isNullOrEmpty(proxy) ? System.getenv("HTTPS_PROXY")
                : proxy;
        if (isNullOrEmpty(proxyInput)) {
            return null;
        }
        try {
            return new URL(proxyInput.matches("^http[s]?://.*$") ? proxyInput
                    : "http://" + proxyInput);
        } catch (MalformedURLException e) {
            log.error("Invalid proxy url {}", proxyInput, e);
            return null;
        }
    }

    private final HttpHost createProxyHttpHost(String proxyUrl) {
        Proxy proxy = createProxy(proxyUrl);
        if (proxy == null || proxy.address() == null) {
            return null;
        }
        if (!(proxy.address() instanceof InetSocketAddress)) {
            throw new RuntimeException(
                    "Detect an unsupported subclass of SocketAddress. Please use the InetSocketAddress or subclass. Actual:"
                            + proxy.address().getClass());
        }
        InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
        return new HttpHost(proxyAddress.getHostName(), proxyAddress.getPort());
    }

    private final BasicCredentialsProvider createBasicCredentialsProvider(
            String proxy, String proxyUser, String proxyPass,
            HttpHost proxyHost) {
        URL proxyUrl = determineProxyUrl(proxy);
        if (proxyUrl == null) {
            return null;
        }
        try {
            String username = null;
            String password = null;

            // apply env value
            String userInfo = proxyUrl.getUserInfo();
            if (userInfo != null) {
                StringTokenizer st = new StringTokenizer(userInfo, ":");
                username = st.hasMoreTokens() ? URLDecoder.decode(
                        st.nextToken(), StandardCharsets.UTF_8.name()) : null;
                password = st.hasMoreTokens() ? URLDecoder.decode(
                        st.nextToken(), StandardCharsets.UTF_8.name()) : null;
            }
            String envProxyUser = System.getenv("HTTPS_PROXY_USER");
            String envProxyPass = System.getenv("HTTPS_PROXY_PASS");
            username = (envProxyUser != null) ? envProxyUser : username;
            password = (envProxyPass != null) ? envProxyPass : password;

            // apply option value
            username = (proxyUser != null) ? proxyUser : username;
            password = (proxyPass != null) ? proxyPass : password;

            if (username == null) {
                return null;
            }

            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(proxyHost.getHostName(), proxyHost.getPort()),
                    new UsernamePasswordCredentials(username, password));
            return credentialsProvider;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid encoding.", e);
        }
    }

    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }

    public static class Builder {

        private String proxy;
        private String proxyUser;
        private String proxyPass;

        public Builder proxy(String proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
            return this;
        }

        public Builder proxyPass(String proxyPass) {
            this.proxyPass = proxyPass;
            return this;
        }

        public WdmHttpClient build() {
            return new WdmHttpClient(this.proxy, this.proxyUser,
                    this.proxyPass);
        }

    }

    private interface Method {
        HttpUriRequest toHttpUriRequest();
    }

    public final static class Get implements Method {
        private final HttpGet get;
        private final RequestConfig config;

        public Get(URL url) {
            this.get = new HttpGet(url.toString());
            this.config = null;
        }

        public Get(String url, int socketTimeout) {
            this.get = new HttpGet(url);
            this.config = RequestConfig.custom().setSocketTimeout(socketTimeout)
                    .build();
        }

        public Get addHeader(String name, String value) {
            this.get.addHeader(name, value);
            return this;
        }

        @Override
        public HttpUriRequest toHttpUriRequest() {
            if (config != null) {
                get.setConfig(config);
            }
            return this.get;
        }

    }

    public final static class Options implements Method {
        private final HttpOptions options;

        public Options(URL url) {
            this.options = new HttpOptions(url.toString());
        }

        @Override
        public HttpUriRequest toHttpUriRequest() {
            return this.options;
        }
    }

    public final static class Response {
        private final HttpResponse response;

        public Response(HttpResponse response) {
            this.response = response;
        }

        public InputStream getContent() throws IOException {
            return this.response.getEntity().getContent();
        }

    }

}
