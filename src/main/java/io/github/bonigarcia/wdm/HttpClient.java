/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.auth.AuthScope.ANY_REALM;
import static org.apache.http.client.config.AuthSchemes.NTLM;
import static org.apache.http.client.config.CookieSpecs.STANDARD;
import static org.apache.http.client.config.RequestConfig.custom;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;

/**
 * HTTP Client.
 *
 * @author Boni Garcia
 * @since 2.1.0
 */
public class HttpClient implements Closeable {

    final Logger log = getLogger(lookup().lookupClass());

    CloseableHttpClient closeableHttpClient;
    int timeout;

    public HttpClient(String proxyUrl, String proxyUser, String proxyPass) {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManagerShared(true);
        try {
            Optional<HttpHost> proxyHost = createProxyHttpHost(proxyUrl);
            if (proxyHost.isPresent()) {
                builder.setProxy(proxyHost.get());
                Optional<BasicCredentialsProvider> credentialsProvider = createBasicCredentialsProvider(
                        proxyUrl, proxyUser, proxyPass, proxyHost.get());
                if (credentialsProvider.isPresent()) {
                    builder.setDefaultCredentialsProvider(
                            credentialsProvider.get());
                }
            }

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext, allHostsValid);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory>create().register("https", sslsf)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);

            builder.setConnectionManager(cm);
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }

        closeableHttpClient = builder.useSystemProperties().build();
    }

    public Optional<Proxy> createProxy(String proxyUrl)
            throws MalformedURLException {
        Optional<URL> url = determineProxyUrl(proxyUrl);
        if (url.isPresent()) {
            String proxyHost = url.get().getHost();
            int proxyPort = url.get().getPort() == -1 ? 80
                    : url.get().getPort();
            return Optional.of(new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, proxyPort)));
        }
        return empty();
    }

    public HttpGet createHttpGet(URL url) {
        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.addHeader("User-Agent", "Mozilla/5.0");
        httpGet.addHeader("Connection", "keep-alive");
        RequestConfig requestConfig = custom().setCookieSpec(STANDARD)
                .setSocketTimeout(timeout).build();
        httpGet.setConfig(requestConfig);
        return httpGet;
    }

    public HttpResponse execute(HttpRequestBase method) throws IOException {
        HttpResponse response = closeableHttpClient.execute(method);
        if (response.getStatusLine().getStatusCode() >= SC_BAD_REQUEST) {
            String errorMessage = "A response error is detected: "
                    + response.getStatusLine();
            log.error(errorMessage);
            throw new WebDriverManagerException(errorMessage);
        }
        return response;
    }

    public boolean isValid(URL url) throws IOException {
        HttpResponse response = closeableHttpClient
                .execute(new HttpOptions(url.toString()));
        if (response.getStatusLine().getStatusCode() > SC_UNAUTHORIZED) {
            log.debug("A response error is detected. {}",
                    response.getStatusLine());
            return false;
        }
        return true;
    }

    private Optional<URL> determineProxyUrl(String proxy)
            throws MalformedURLException {
        String proxyInput = isNullOrEmpty(proxy) ? getenv("HTTPS_PROXY")
                : proxy;
        if (!isNullOrEmpty(proxyInput)) {
            return Optional.of(
                    new URL(proxyInput.matches("^http[s]?://.*$") ? proxyInput
                            : "http://" + proxyInput));
        }
        return empty();
    }

    private final Optional<HttpHost> createProxyHttpHost(String proxyUrl)
            throws MalformedURLException {
        Optional<Proxy> proxy = createProxy(proxyUrl);
        if (proxy.isPresent() && proxy.get().address() != null) {
            if (!(proxy.get().address() instanceof InetSocketAddress)) {
                String errorMessage = "Detect an unsupported subclass of SocketAddress. "
                        + "Please use the InetSocketAddress or subclass. Actual:"
                        + proxy.get().address().getClass();
                log.error(errorMessage);
                throw new WebDriverManagerException(errorMessage);
            }
            InetSocketAddress proxyAddress = (InetSocketAddress) proxy.get()
                    .address();
            return Optional.of(new HttpHost(proxyAddress.getHostName(),
                    proxyAddress.getPort()));
        }
        return empty();
    }

    private final Optional<BasicCredentialsProvider> createBasicCredentialsProvider(
            String proxy, String proxyUser, String proxyPass,
            HttpHost proxyHost)
            throws MalformedURLException, UnsupportedEncodingException {
        Optional<URL> proxyUrl = determineProxyUrl(proxy);
        if (!proxyUrl.isPresent()) {
            return empty();
        }
        String username = null;
        String password = null;

        // apply env value
        String userInfo = proxyUrl.get().getUserInfo();
        if (userInfo != null) {
            StringTokenizer st = new StringTokenizer(userInfo, ":");
            username = st.hasMoreTokens() ? decode(st.nextToken(), UTF_8.name())
                    : null;
            password = st.hasMoreTokens() ? decode(st.nextToken(), UTF_8.name())
                    : null;
        }
        String envProxyUser = getenv("HTTPS_PROXY_USER");
        String envProxyPass = getenv("HTTPS_PROXY_PASS");
        username = (envProxyUser != null) ? envProxyUser : username;
        password = (envProxyPass != null) ? envProxyPass : password;

        // apply option value
        username = (proxyUser != null) ? proxyUser : username;
        password = (proxyPass != null) ? proxyPass : password;

        if (username == null) {
            return empty();
        }

        String ntlmUsername = username;
        String ntlmDomain = null;

        int index = username.indexOf('\\');
        if (index > 0) {
            ntlmDomain = username.substring(0, index);
            ntlmUsername = username.substring(index + 1);
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(proxyHost.getHostName(),
                proxyHost.getPort(), ANY_REALM, NTLM);
        Credentials creds = new NTCredentials(ntlmUsername, password,
                getWorkstation(), ntlmDomain);
        credentialsProvider.setCredentials(authScope, creds);

        authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort());
        creds = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(authScope, creds);

        return Optional.of(credentialsProvider);
    }

    private String getWorkstation() {
        Map<String, String> env = getenv();

        if (env.containsKey("COMPUTERNAME")) {
            // Windows
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            // Unix/Linux/MacOS
            return env.get("HOSTNAME");
        } else {
            // From DNS
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                return null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        closeableHttpClient.close();
    }

    public void setTimeout(int timeout) {
        this.timeout = (int) SECONDS.toMillis(timeout);
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

        public HttpClient build() {
            return new HttpClient(proxy, proxyUser, proxyPass);
        }
    }

}
