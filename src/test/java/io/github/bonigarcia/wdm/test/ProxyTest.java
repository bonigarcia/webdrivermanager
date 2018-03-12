/*		
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)		
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
package io.github.bonigarcia.wdm.test;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.auth.AuthScope.ANY_REALM;
import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.apache.http.client.config.AuthSchemes.KERBEROS;
import static org.apache.http.client.config.AuthSchemes.NTLM;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.Optional;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.Config;
import io.github.bonigarcia.wdm.HttpClient;
import io.github.bonigarcia.wdm.WebDriverManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * Test for proxy.
 * 
 * @since 1.6.1
 */
@RunWith(JMockit.class)
public class ProxyTest {

    final Logger log = getLogger(lookup().lookupClass());

    private static final String PROXY_URL = "my.http.proxy";
    private static final String PROXY_PORT = "1234";

    private static final String[] PROXYS_TEST_STRINGS = {
            PROXY_URL + ":" + PROXY_PORT, PROXY_URL, "http://" + PROXY_URL,
            "http://" + PROXY_URL + ":" + PROXY_PORT,
            "https://" + PROXY_URL + ":" + PROXY_PORT,
            "https://test:test@" + PROXY_URL + ":" + PROXY_PORT, };

    @AfterClass
    public static void teardown() {
        WebDriverManager.config().reset();
    }

    @Test
    public void testRealEnvProxyToNull() throws Exception {
        WebDriverManager browserManager = ChromeDriverManager.getInstance();
        setSystemGetEnvMock(null);
        assertFalse(getProxy(browserManager).isPresent());
    }

    @Test
    public void testRealEnvProxyToNotNull() throws Exception {
        WebDriverManager browserManager = ChromeDriverManager.getInstance();
        setSystemGetEnvMock(PROXY_URL);

        InetSocketAddress address = (InetSocketAddress) getProxy(browserManager)
                .get().address();
        assertThat(address.getHostName(), equalTo(PROXY_URL));
    }

    @Test
    public void testProxyCredentialsScope() throws Exception {
        WebDriverManager.config().setProxy("myproxy:8081")
                .setProxyUser("domain\\me").setProxyPass("pass");
        HttpClient wdmClient = new HttpClient();
        Field field = HttpClient.class.getDeclaredField("closeableHttpClient");
        field.setAccessible(true);

        CloseableHttpClient client = (CloseableHttpClient) field.get(wdmClient);
        field = client.getClass().getDeclaredField("credentialsProvider");
        field.setAccessible(true);

        BasicCredentialsProvider provider = (BasicCredentialsProvider) field
                .get(client);

        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, NTLM)),
                instanceOf(NTCredentials.class));
        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, BASIC)),
                instanceOf(UsernamePasswordCredentials.class));
        assertThat(provider.getCredentials(new AuthScope(ANY_HOST, ANY_PORT)),
                instanceOf(UsernamePasswordCredentials.class));
        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, KERBEROS)),
                instanceOf(UsernamePasswordCredentials.class));
    }

    @Test
    public void testProxyCredentials() throws Exception {
        WebDriverManager.config().setProxy("myproxy:8081")
                .setProxyUser("domain\\me").setProxyPass("pass");
        HttpClient wdmClient = new HttpClient();
        Field field = HttpClient.class.getDeclaredField("closeableHttpClient");
        field.setAccessible(true);

        CloseableHttpClient client = (CloseableHttpClient) field.get(wdmClient);
        field = client.getClass().getDeclaredField("credentialsProvider");
        field.setAccessible(true);

        BasicCredentialsProvider provider = (BasicCredentialsProvider) field
                .get(client);

        NTCredentials ntcreds = (NTCredentials) provider.getCredentials(
                new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, NTLM));
        assertThat(ntcreds.getDomain(), equalTo("DOMAIN"));
        assertThat(ntcreds.getUserName(), equalTo("me"));
        assertThat(ntcreds.getPassword(), equalTo("pass"));

        UsernamePasswordCredentials creds = (UsernamePasswordCredentials) provider
                .getCredentials(new AuthScope(ANY_HOST, ANY_PORT));
        assertThat(creds.getUserName(), equalTo("domain\\me"));
        assertThat(creds.getPassword(), equalTo("pass"));
    }

    @Test
    public void testMockedEnvProxy() throws Exception {
        for (String proxyTestString : PROXYS_TEST_STRINGS) {
            setSystemGetEnvMock(proxyTestString);

            log.info("Testing proxy {}", proxyTestString);

            WebDriverManager browserManager = ChromeDriverManager.getInstance();
            InetSocketAddress address = (InetSocketAddress) getProxy(
                    browserManager).get().address();
            assertThat(address.getHostName(), equalTo(PROXY_URL));
        }
    }

    private MockUp<System> setSystemGetEnvMock(final String httpProxyString) {
        MockUp<System> mockUp = new MockUp<System>() {
            @Mock
            public String getenv(final String string) {
                return httpProxyString;
            }
        };
        return mockUp;
    }

    private Optional<Proxy> getProxy(WebDriverManager browserManager)
            throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException,
            MalformedURLException {
        Field httpClientField = WebDriverManager.class
                .getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(browserManager, new HttpClient());

        Field configField = WebDriverManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        Config config = (Config) configField.get(browserManager);
        String proxy = config.getProxy();
        HttpClient wdmHttpClient = (HttpClient) httpClientField
                .get(browserManager);
        return wdmHttpClient.createProxy(proxy);
    }

}
