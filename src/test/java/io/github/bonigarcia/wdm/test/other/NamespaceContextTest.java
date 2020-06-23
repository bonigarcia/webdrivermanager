package io.github.bonigarcia.wdm.test.other;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.online.S3BucketListNamespaceContext;
import org.junit.Test;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class NamespaceContextTest {

    @Test
    public void testS3BucketListNamespaceContext() throws IOException {
        TestWebDriverManager testManager = new TestWebDriverManager();
        List<URL> urls = testManager.getDriverUrls();
        assertThat(urls, is(not(empty())));
    }

    private static final class TestWebDriverManager extends WebDriverManager{

        @Override
        protected NamespaceContext getNamespaceContext() {
            return new S3BucketListNamespaceContext();
        }

        @Override
        protected List<URL> getDriverUrls() throws IOException {
            httpClient = new HttpClient(config());
            return getDriversFromXml(getDriverUrl(), "//s3:Contents/s3:Key");
        }

        @Override
        protected Optional<String> getBrowserVersionFromTheShell() {
            return Optional.empty();
        }

        @Override
        protected String getDriverName() {
            return null;
        }

        @Override
        protected String getDriverVersion() {
            return null;
        }

        @Override
        protected void setDriverVersion(String driverVersion) {

        }

        @Override
        protected String getBrowserVersion() {
            return null;
        }

        @Override
        protected void setBrowserVersion(String browserVersion) {

        }

        @Override
        protected void setDriverUrl(URL url) {

        }

        @Override
        protected URL getDriverUrl() {
            return getDriverUrlCkeckingMirror(config().getChromeDriverUrl());
        }

        @Override
        protected Optional<URL> getMirrorUrl() {
            return Optional.empty();
        }

        @Override
        protected Optional<String> getExportParameter() {
            return Optional.empty();
        }

        @Override
        public DriverManagerType getDriverManagerType() {
            return null;
        }

    }

}
