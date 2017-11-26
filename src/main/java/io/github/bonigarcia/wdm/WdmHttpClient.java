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

import static io.github.bonigarcia.wdm.WdmConfig.isNullOrEmpty;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.auth.AuthScope.ANY_REALM;
import static org.apache.http.client.config.AuthSchemes.NTLM;
import static org.apache.http.client.config.RequestConfig.custom;
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
import java.util.Map;
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
import org.apache.http.client.methods.HttpUriRequest;
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
 * The Http Client for WebDriverManager.
 *
 * @author Kazuki Shimizu
 * @since 1.6.2
 */
public class WdmHttpClient implements Closeable {
    final Logger log = getLogger(lookup().lookupClass());

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

        try {
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

        httpClient = builder.useSystemProperties().build();
    }

    public Proxy createProxy(String proxyUrl) {
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
        if (response.getStatusLine().getStatusCode() >= SC_BAD_REQUEST) {
            String errorMessage = "A response error is detected: "
                    + response.getStatusLine();
            log.error(errorMessage);
            throw new WebDriverManagerException(errorMessage);
        }
        return new Response(response);
    }

    public boolean isValid(URL url) throws IOException {
        HttpResponse response = httpClient
                .execute(new WdmHttpClient.Options(url).toHttpUriRequest());
        if (response.getStatusLine().getStatusCode() > SC_UNAUTHORIZED) {
            log.debug("A response error is detected. {}",
                    response.getStatusLine());
            return false;
        }
        return true;
    }

    private URL determineProxyUrl(String proxy) {
        String proxyInput = isNullOrEmpty(proxy) ? getenv("HTTPS_PROXY")
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
            String errorMessage = "Detect an unsupported subclass of SocketAddress. "
                    + "Please use the InetSocketAddress or subclass. Actual:"
                    + proxy.address().getClass();
            log.error(errorMessage);
            throw new WebDriverManagerException(errorMessage);
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
                username = st.hasMoreTokens()
                        ? decode(st.nextToken(), UTF_8.name())
                        : null;
                password = st.hasMoreTokens()
                        ? decode(st.nextToken(), UTF_8.name())
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
                return null;
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

            authScope = new AuthScope(proxyHost.getHostName(),
                    proxyHost.getPort());
            creds = new UsernamePasswordCredentials(username, password);
            credentialsProvider.setCredentials(authScope, creds);

            return credentialsProvider;
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Invalid encoding creating credentials for proxy "
                    + proxy;
            log.error(errorMessage, e);
            throw new WebDriverManagerException(errorMessage, e);
        }
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
        httpClient.close();
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
            return new WdmHttpClient(proxy, proxyUser, proxyPass);
        }
    }

    private interface Method {
        HttpUriRequest toHttpUriRequest();
    }

    public static final class Get implements Method {
        private final HttpGet httpGet;
        private final RequestConfig requestConfig;

        public Get(URL url) {
            httpGet = new HttpGet(url.toString());
            requestConfig = null;
        }

        public Get(String url, int socketTimeout) {
            httpGet = new HttpGet(url);
            requestConfig = custom().setSocketTimeout(socketTimeout)
                    .build();
        }

        public Get addHeader(String name, String value) {
            httpGet.addHeader(name, value);
            return this;
        }

        @Override
        public HttpUriRequest toHttpUriRequest() {
            if (requestConfig != null) {
                httpGet.setConfig(requestConfig);
            }
            return httpGet;
        }
    }

    public static final class Options implements Method {
        private final HttpOptions httpOptions;

        public Options(URL url) {
            httpOptions = new HttpOptions(url.toString());
        }

        @Override
        public HttpUriRequest toHttpUriRequest() {
            return httpOptions;
        }
    }

    public static final class Response {
        private final HttpResponse httpResponse;

        public Response(HttpResponse response) {
            this.httpResponse = response;
        }

        public InputStream getContent() throws IOException {
            return httpResponse.getEntity().getContent();
        }
    }

}
