package io.github.bonigarcia.wdm;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.online.Downloader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerTest {

    private static final String DUMMY_URL = "http://not.important";

    @InjectMocks
    private DummyWebDriverManager webDriverManager;

    @Mock
    private Config config;

    @Mock
    private Downloader downloader;

    @Test
    @DisplayName("avoidExternalConnections() should fluently set to true the corresponding config parameter")
    void avoidExternalConnections() {
        when(config.setAvoidExternalConnections(true)).thenReturn(config);

        assertEquals(webDriverManager,
                webDriverManager.avoidExternalConnections());
    }

    @DisplayName("download")
    @ParameterizedTest(name = "with driver version {0} should download version {1}")
    @ValueSource(strings = { "123", ".123" })
    void download(String driverVersion) throws IOException {
        String expected = "expected";
        String cleanDriverVersion = "123";

        when(config.isAvoidExternalConnections()).thenReturn(true);

        when(downloader.download(new URL(DUMMY_URL), cleanDriverVersion,
                "dummy", null)).thenReturn(expected);

        assertEquals(expected, webDriverManager.download(driverVersion));
    }

    private static class DummyWebDriverManager extends WebDriverManager {

        @Override
        protected List<URL> getDriverUrls(String driverVersion) {
            return null;
        }

        @Override
        protected String getDriverName() {
            return "dummy";
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
            return null;
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

        @Override
        public WebDriverManager exportParameter(String exportParameter) {
            return null;
        }

        @Override
        protected Optional<URL> buildUrl(String driverVersion) {
            try {
                return Optional.of(new URL(DUMMY_URL));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
