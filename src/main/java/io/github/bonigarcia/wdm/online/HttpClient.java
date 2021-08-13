/*
 * (C) Copyright 2015 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.online;

import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static org.apache.hc.client5.http.auth.StandardAuthScheme.NTLM;
import static org.apache.hc.client5.http.config.RequestConfig.custom;
import static org.apache.hc.client5.http.cookie.StandardCookieSpec.STRICT;
import static org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.DeflateInputStreamFactory;
import org.apache.hc.client5.http.entity.GZIPInputStreamFactory;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.brotli.dec.BrotliInputStream;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * HTTP Client.
 *
 * @author Boni Garcia
 * @since 2.1.0
 */
public class HttpClient implements Closeable {

    final Logger log = getLogger(lookup().lookupClass());

    Config config;
    CloseableHttpClient closeableHttpClient;

    public HttpClient(Config config) {
        this.config = config;

        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManagerShared(true);
        try {
            setupProxyIfRequired(builder);

            HostnameVerifier allHostsValid = (hostname, session) -> hostname
                    .equalsIgnoreCase(session.getPeerHost());
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
            
            // Add decompression handlers
            final LinkedHashMap<String, InputStreamFactory> contentDecoderMap = new LinkedHashMap<>();
            contentDecoderMap.put("br", new InputStreamFactory() {
                @Override
                public InputStream create(InputStream inStream) throws IOException {
                    return new BrotliInputStream(inStream);
                }
            });
            contentDecoderMap.put("gzip", GZIPInputStreamFactory.getInstance());
            contentDecoderMap.put("x-gzip", GZIPInputStreamFactory.getInstance());
            contentDecoderMap.put("deflate", DeflateInputStreamFactory.getInstance());
            builder.setContentDecoderRegistry(contentDecoderMap);

            builder.setConnectionManager(cm);
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }

        closeableHttpClient = builder.useSystemProperties().build();
    }

    public HttpGet createHttpGet(URL url) {
        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.addHeader("user-agent", "Apache-HttpClient/5.0");
        httpGet.addHeader("accept-encoding", "gzip, deflate, br");
        httpGet.addHeader("cache-control", "max-age=0");
        httpGet.addHeader("connection", "keep-alive");

        RequestConfig requestConfig = custom().setCookieSpec(STRICT)
                .setConnectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
        httpGet.setConfig(requestConfig);
        return httpGet;
    }

    public CloseableHttpResponse execute(ClassicHttpRequest method)
            throws IOException {
        CloseableHttpResponse response = closeableHttpClient.execute(method);
        int responseCode = response.getCode();
        if (responseCode >= SC_BAD_REQUEST) {
            String errorMessage;
            String methodUri = "";
            try {
                methodUri = method.getUri().toString();
            } catch (Exception e) {
                log.trace("Exception reading URI from method: {}",
                        e.getMessage());
            }
            errorMessage = "Error HTTP " + responseCode + " executing "
                    + methodUri;
            log.error(errorMessage);
            throw new WebDriverManagerException(errorMessage);
        }
        return response;
    }

    private void setupProxyIfRequired(HttpClientBuilder builder)
            throws MalformedURLException, UnsupportedEncodingException {
        String proxy = config.getProxy();
        Optional<HttpHost> proxyHost = createProxyHttpHost(proxy);

        if (proxyHost.isPresent()) {
            builder.setProxy(proxyHost.get());
            Optional<BasicCredentialsProvider> credentialsProvider = createBasicCredentialsProvider(
                    proxy, config.getProxyUser(), config.getProxyPass(),
                    proxyHost.get());
            if (credentialsProvider.isPresent()) {
                builder.setDefaultCredentialsProvider(
                        credentialsProvider.get());
            }
        }
    }

    private Optional<Proxy> createProxy(String proxy)
            throws MalformedURLException {
        Optional<URL> url = determineProxyUrl(proxy);
        if (url.isPresent()) {
            String proxyHost = url.get().getHost();
            int proxyPort = url.get().getPort() == -1 ? 80
                    : url.get().getPort();
            return Optional.of(new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, proxyPort)));
        }
        return empty();
    }

    private Optional<URL> determineProxyUrl(String proxy)
            throws MalformedURLException {
        String proxyFromEnvCaps = getenv("HTTPS_PROXY");
        String proxyFromEnv = isNullOrEmpty(proxyFromEnvCaps)
                ? getenv("https_proxy")
                : proxyFromEnvCaps;
        String proxyInput = isNullOrEmpty(proxy) ? proxyFromEnv : proxy;
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
        username = isNullOrEmpty(proxyUser) ? username : proxyUser;
        password = isNullOrEmpty(proxyPass) ? password : proxyPass;

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

        AuthScope authScope = new AuthScope(proxyHost, null, NTLM);
        char[] passwd = (password == null) ? new char[0]
                : password.toCharArray();

        Credentials creds = new NTCredentials(ntlmUsername, passwd,
                getWorkstation(), ntlmDomain);
        credentialsProvider.setCredentials(authScope, creds);

        authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort());
        creds = new UsernamePasswordCredentials(username, passwd);
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

}
