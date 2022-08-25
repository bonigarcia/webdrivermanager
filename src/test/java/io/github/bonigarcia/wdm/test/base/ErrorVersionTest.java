package io.github.bonigarcia.wdm.test.base;

import org.junit.jupiter.api.Test;

import java.io.File;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorVersionTest {
    @Test
    void testCustomProperties() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        wdm.config().setProperties("wdm-error-test.properties");
        wdm.setup();
        String driverPath = wdm.getDownloadedDriverPath();
        assertThat(driverPath).isNotNull();
        File driver = new File(driverPath);
        assertThat(driver).exists();

        wdm.config().isAvoidFallback();
    }
}
